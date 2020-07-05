package fr.jorisfavier.youshallnotpass.di

import android.app.Application
import android.content.ClipboardManager
import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import fr.jorisfavier.youshallnotpass.YouShallNotPassDatabase
import fr.jorisfavier.youshallnotpass.data.ItemDataSource
import fr.jorisfavier.youshallnotpass.manager.ICryptoManager
import fr.jorisfavier.youshallnotpass.manager.impl.CryptoManager
import fr.jorisfavier.youshallnotpass.repository.IItemRepository
import fr.jorisfavier.youshallnotpass.repository.impl.ItemRepository
import javax.inject.Singleton


@Module(includes = [ViewModelModule::class])
class AppModule {

    @Singleton
    @Provides
    fun provideItemRepository(itemDataSource: ItemDataSource): IItemRepository {
        return ItemRepository(itemDataSource)
    }

    @Singleton
    @Provides
    fun provideDb(app: Application): YouShallNotPassDatabase {
        return Room.databaseBuilder(app, YouShallNotPassDatabase::class.java, "ysnp.db")
                .fallbackToDestructiveMigration()
                .build()
    }

    @Singleton
    @Provides
    fun provideItemDataSource(db: YouShallNotPassDatabase): ItemDataSource {
        return db.itemDao()
    }

    @Singleton
    @Provides
    fun provideCryptoManager(): ICryptoManager {
        return CryptoManager()
    }

    @Provides
    fun provideClipboardManager(app: Application): ClipboardManager {
        return app.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }
}