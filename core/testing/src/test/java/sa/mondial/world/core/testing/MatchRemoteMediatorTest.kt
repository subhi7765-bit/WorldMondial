package sa.mondial.world.core.testing

import android.content.Context
import androidx.paging.*
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import sa.mondial.world.core.database.MondialDatabase
import sa.mondial.world.core.database.dao.MatchDao
import sa.mondial.world.core.database.entity.MatchEntity as RealMatchEntity
import sa.mondial.world.core.domain.Match
import sa.mondial.world.feature.matches.data.MatchRemoteMediator
import sa.mondial.world.core.network.api.MatchApiService
import sa.mondial.world.core.network.dto.MatchDto
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalPagingApi::class)
@RunWith(RobolectricTestRunner::class)
class MatchRemoteMediatorTest {

    private val testDispatcher = StandardTestDispatcher()
    
    // Core under test inputs / Mock dependencies 
    private val mockDao: MatchDao = mockk(relaxed = true)
    private val mockKeysDao: sa.mondial.world.core.database.dao.MatchRemoteKeysDao = mockk(relaxed = true)
    private val mockApi: sa.mondial.world.core.network.api.MatchApiService = mockk(relaxed = true)
    
    // Simulated Android Environment & Real in-memory DB components for verification
    private lateinit var inMemoryDb: MondialDatabase
    private lateinit var realMatchDao: MatchDao
    private lateinit var mockWebServer: MockWebServer
    private lateinit var okHttpClient: OkHttpClient

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        
        // 1. In-Memory Room Database initialization
        inMemoryDb = Room.inMemoryDatabaseBuilder(
            context,
            MondialDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
        realMatchDao = inMemoryDb.matchDao()

        // 2. MockWebServer setup acting as the remote Rest API
        mockWebServer = MockWebServer()
        mockWebServer.start()

        // OkHttpClient configuration with a custom interceptor (as requested)
        okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request()
                // Custom behavior / analytics logging could go here
                chain.proceed(request)
            }
            .build()
    }

    @After
    fun tearDown() {
        inMemoryDb.close()
        mockWebServer.shutdown()
    }

    @Test
    fun refreshLoadReturnsSuccessAndEndOfPaginationWhenNoMoreData() = runTest(testDispatcher) {
        val mediator = MatchRemoteMediator(
            localDatabaseDao = mockDao,
            remoteKeysDao = mockKeysDao,
            remoteNetworkApi = mockApi,
            forceRefresh = true
        )

        val pagingState = PagingState<Int, RealMatchEntity>(
            pages = listOf(),
            anchorPosition = null,
            config = PagingConfig(pageSize = 10),
            leadingPlaceholderCount = 0
        )

        // Return empty result to signal end of pagination
        coEvery { mockApi.fetchPagedMatches(any(), any()) } returns emptyList()

        val result = mediator.load(LoadType.REFRESH, pagingState)
        advanceUntilIdle()

        assertTrue(result is RemoteMediator.MediatorResult.Success)
        assertTrue((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
    }

    @Test
    fun refreshLoadReturnsSuccessAndMoreDataExist() = runTest(testDispatcher) {
        val mediator = MatchRemoteMediator(
            localDatabaseDao = mockDao,
            remoteKeysDao = mockKeysDao,
            remoteNetworkApi = mockApi,
            forceRefresh = true
        )

        val pagingState = PagingState<Int, RealMatchEntity>(
            pages = listOf(),
            anchorPosition = null,
            config = PagingConfig(pageSize = 10),
            leadingPlaceholderCount = 0
        )

        coEvery { mockApi.fetchPagedMatches(any(), any()) } returns listOf(
            MatchDto("match-01", "السعودية", "Saudi Arabia", "الأرجنتين", "Argentina")
        )

        val result = mediator.load(LoadType.REFRESH, pagingState)
        advanceUntilIdle()

        assertTrue(result is RemoteMediator.MediatorResult.Success)
        assertFalse((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
        coVerify(exactly = 1) { mockDao.refreshAllMatches(any()) }
    }

    @Test
    fun appendLoadReturnsSuccessAndEndOfPaginationReached() = runTest(testDispatcher) {
        val mediator = MatchRemoteMediator(
            localDatabaseDao = mockDao,
            remoteKeysDao = mockKeysDao,
            remoteNetworkApi = mockApi,
            forceRefresh = false
        )

        val pagingState = PagingState<Int, RealMatchEntity>(
            pages = listOf(
                PagingSource.LoadResult.Page(
                    data = listOf(RealMatchEntity("match-01", "السعودية", "Saudi Arabia", "الأرجنتين", "Argentina", 2, 1, "FINISHED", "المجموعات", "Group stage", "2022-11-22T10:00:00Z")),
                    prevKey = null,
                    nextKey = 2
                )
            ),
            anchorPosition = null,
            config = PagingConfig(pageSize = 1),
            leadingPlaceholderCount = 0
        )

        // Mock remote empty list indicating append finished at the end
        coEvery { mockApi.fetchPagedMatches(any(), any()) } returns emptyList()

        val result = mediator.load(LoadType.APPEND, pagingState)
        advanceUntilIdle()

        assertTrue(result is RemoteMediator.MediatorResult.Success)
        assertTrue((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
        coVerify(exactly = 1) { mockDao.insertMatchesBatch(any()) }
    }

    @Test
    fun loadReturnsErrorStateWhenNetworkCallFails() = runTest(testDispatcher) {
        val mediator = MatchRemoteMediator(
            localDatabaseDao = mockDao,
            remoteKeysDao = mockKeysDao,
            remoteNetworkApi = mockApi,
            forceRefresh = true
        )

        val pagingState = PagingState<Int, RealMatchEntity>(
            pages = listOf(),
            anchorPosition = null,
            config = PagingConfig(pageSize = 10),
            leadingPlaceholderCount = 0
        )

        // Mock HTTP exception to simulate a networking/service offline scenario
        coEvery { mockApi.fetchPagedMatches(any(), any()) } throws IOException("No internet connection")

        val result = mediator.load(LoadType.REFRESH, pagingState)
        advanceUntilIdle()

        assertTrue(result is RemoteMediator.MediatorResult.Error)
    }
}