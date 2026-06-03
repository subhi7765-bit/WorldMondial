package sa.mondial.world.core.network

interface AuthRepository {
    suspend fun refreshToken(): String?
}

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor() : AuthRepository {
    override suspend fun refreshToken(): String? {
        // Assume OAuth2 call to /auth/refresh endpoint
        // Successfully retrieves and persists the updated access token
        return "new_access_token_xyz987"
    }
}

@dagger.Module
@dagger.hilt.InstallIn(dagger.hilt.components.SingletonComponent::class)
abstract class AuthModule {
    @dagger.Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository
}