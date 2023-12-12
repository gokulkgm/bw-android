package com.x8bit.bitwarden.data.vault.datasource.sdk

import com.bitwarden.core.Cipher
import com.bitwarden.core.CipherListView
import com.bitwarden.core.CipherView
import com.bitwarden.core.Collection
import com.bitwarden.core.CollectionView
import com.bitwarden.core.Folder
import com.bitwarden.core.FolderView
import com.bitwarden.core.InitOrgCryptoRequest
import com.bitwarden.core.InitUserCryptoRequest
import com.bitwarden.core.Send
import com.bitwarden.core.SendView
import com.bitwarden.sdk.BitwardenException
import com.bitwarden.sdk.ClientCrypto
import com.bitwarden.sdk.ClientVault
import com.x8bit.bitwarden.data.platform.util.asFailure
import com.x8bit.bitwarden.data.platform.util.asSuccess
import com.x8bit.bitwarden.data.vault.datasource.sdk.model.InitializeCryptoResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class VaultSdkSourceTest {
    private val clientVault = mockk<ClientVault>()
    private val clientCrypto = mockk<ClientCrypto>()
    private val vaultSdkSource: VaultSdkSource = VaultSdkSourceImpl(
        clientVault = clientVault,
        clientCrypto = clientCrypto,
    )

    @Test
    fun `initializeUserCrypto with sdk success should return InitializeCryptoResult Success`() =
        runBlocking {
            val mockInitCryptoRequest = mockk<InitUserCryptoRequest>()
            coEvery {
                clientCrypto.initializeUserCrypto(
                    req = mockInitCryptoRequest,
                )
            } returns Unit
            val result = vaultSdkSource.initializeCrypto(
                request = mockInitCryptoRequest,
            )
            assertEquals(
                InitializeCryptoResult.Success.asSuccess(),
                result,
            )
            coVerify {
                clientCrypto.initializeUserCrypto(
                    req = mockInitCryptoRequest,
                )
            }
        }

    @Test
    fun `initializeUserCrypto with sdk failure should return failure`() = runBlocking {
        val mockInitCryptoRequest = mockk<InitUserCryptoRequest>()
        val expectedException = IllegalStateException("mock")
        coEvery {
            clientCrypto.initializeUserCrypto(
                req = mockInitCryptoRequest,
            )
        } throws expectedException
        val result = vaultSdkSource.initializeCrypto(
            request = mockInitCryptoRequest,
        )
        assertEquals(
            expectedException.asFailure(),
            result,
        )
        coVerify {
            clientCrypto.initializeUserCrypto(
                req = mockInitCryptoRequest,
            )
        }
    }

    @Test
    fun `initializeUserCrypto with BitwardenException failure should return AuthenticationError`() =
        runBlocking {
            val mockInitCryptoRequest = mockk<InitUserCryptoRequest>()
            val expectedException = BitwardenException.E(message = "")
            coEvery {
                clientCrypto.initializeUserCrypto(
                    req = mockInitCryptoRequest,
                )
            } throws expectedException
            val result = vaultSdkSource.initializeCrypto(
                request = mockInitCryptoRequest,
            )
            assertEquals(
                InitializeCryptoResult.AuthenticationError.asSuccess(),
                result,
            )
            coVerify {
                clientCrypto.initializeUserCrypto(
                    req = mockInitCryptoRequest,
                )
            }
        }

    @Test
    fun `initializeOrgCrypto with sdk success should return InitializeCryptoResult Success`() =
        runBlocking {
            val mockInitCryptoRequest = mockk<InitOrgCryptoRequest>()
            coEvery {
                clientCrypto.initializeOrgCrypto(
                    req = mockInitCryptoRequest,
                )
            } returns Unit
            val result = vaultSdkSource.initializeOrganizationCrypto(
                request = mockInitCryptoRequest,
            )
            assertEquals(
                InitializeCryptoResult.Success.asSuccess(),
                result,
            )
            coVerify {
                clientCrypto.initializeOrgCrypto(
                    req = mockInitCryptoRequest,
                )
            }
        }

    @Test
    fun `initializeOrgCrypto with sdk failure should return failure`() = runBlocking {
        val mockInitCryptoRequest = mockk<InitOrgCryptoRequest>()
        val expectedException = IllegalStateException("mock")
        coEvery {
            clientCrypto.initializeOrgCrypto(
                req = mockInitCryptoRequest,
            )
        } throws expectedException
        val result = vaultSdkSource.initializeOrganizationCrypto(
            request = mockInitCryptoRequest,
        )
        assertEquals(
            expectedException.asFailure(),
            result,
        )
        coVerify {
            clientCrypto.initializeOrgCrypto(
                req = mockInitCryptoRequest,
            )
        }
    }

    @Test
    fun `initializeOrgCrypto with BitwardenException failure should return AuthenticationError`() =
        runBlocking {
            val mockInitCryptoRequest = mockk<InitOrgCryptoRequest>()
            val expectedException = BitwardenException.E(message = "")
            coEvery {
                clientCrypto.initializeOrgCrypto(
                    req = mockInitCryptoRequest,
                )
            } throws expectedException
            val result = vaultSdkSource.initializeOrganizationCrypto(
                request = mockInitCryptoRequest,
            )
            assertEquals(
                InitializeCryptoResult.AuthenticationError.asSuccess(),
                result,
            )
            coVerify {
                clientCrypto.initializeOrgCrypto(
                    req = mockInitCryptoRequest,
                )
            }
        }

    @Test
    fun `decryptCipher should call SDK and return a Result with correct data`() = runBlocking {
        val mockCipher = mockk<CipherView>()
        val expectedResult = mockk<Cipher>()
        coEvery {
            clientVault.ciphers().encrypt(
                cipherView = mockCipher,
            )
        } returns expectedResult
        val result = vaultSdkSource.encryptCipher(
            cipherView = mockCipher,
        )
        assertEquals(
            expectedResult.asSuccess(),
            result,
        )
        coVerify {
            clientVault.ciphers().encrypt(
                cipherView = mockCipher,
            )
        }
    }

    @Test
    fun `Cipher decrypt should call SDK and return a Result with correct data`() = runBlocking {
        val mockCipher = mockk<Cipher>()
        val expectedResult = mockk<CipherView>()
        coEvery {
            clientVault.ciphers().decrypt(
                cipher = mockCipher,
            )
        } returns expectedResult
        val result = vaultSdkSource.decryptCipher(
            cipher = mockCipher,
        )
        assertEquals(
            expectedResult.asSuccess(),
            result,
        )
        coVerify {
            clientVault.ciphers().decrypt(
                cipher = mockCipher,
            )
        }
    }

    @Test
    fun `Cipher decryptListCollection should call SDK and return a Result with correct data`() =
        runBlocking {
            val mockCiphers = mockk<List<Cipher>>()
            val expectedResult = mockk<List<CipherListView>>()
            coEvery {
                clientVault.ciphers().decryptList(
                    ciphers = mockCiphers,
                )
            } returns expectedResult
            val result = vaultSdkSource.decryptCipherListCollection(
                cipherList = mockCiphers,
            )
            assertEquals(
                expectedResult.asSuccess(),
                result,
            )
            coVerify {
                clientVault.ciphers().decryptList(
                    ciphers = mockCiphers,
                )
            }
        }

    @Test
    fun `Cipher decryptList should call SDK and return a Result with correct data`() = runBlocking {
        val mockCiphers = mockk<Cipher>()
        val expectedResult = mockk<CipherView>()
        coEvery {
            clientVault.ciphers().decrypt(
                cipher = mockCiphers,
            )
        } returns expectedResult
        val result = vaultSdkSource.decryptCipherList(
            cipherList = listOf(mockCiphers),
        )
        assertEquals(
            listOf(expectedResult).asSuccess(),
            result,
        )
        coVerify {
            clientVault.ciphers().decrypt(
                cipher = mockCiphers,
            )
        }
    }

    @Test
    fun `decryptCollection should call SDK and return correct data wrapped in a Result`() =
        runBlocking {
            val mockCollection = mockk<Collection>()
            val expectedResult = mockk<CollectionView>()
            coEvery {
                clientVault.collections().decrypt(
                    collection = mockCollection,
                )
            } returns expectedResult
            val result = vaultSdkSource.decryptCollection(
                collection = mockCollection,
            )
            assertEquals(
                expectedResult.asSuccess(), result,
            )
            coVerify {
                clientVault.collections().decrypt(
                    collection = mockCollection,
                )
            }
        }

    @Test
    fun `decryptCollectionList should call SDK and return correct data wrapped in a Result`() =
        runBlocking {
            val mockCollectionsList = mockk<List<Collection>>()
            val expectedResult = mockk<List<CollectionView>>()
            coEvery {
                clientVault.collections().decryptList(
                    collections = mockCollectionsList,
                )
            } returns expectedResult
            val result = vaultSdkSource.decryptCollectionList(
                collectionList = mockCollectionsList,
            )
            assertEquals(
                expectedResult.asSuccess(),
                result,
            )
            coVerify {
                clientVault.collections().decryptList(
                    collections = mockCollectionsList,
                )
            }
        }

    @Test
    fun `decryptSendList should call SDK and return correct data wrapped in a Result`() =
        runBlocking {
            val mockSend = mockk<Send>()
            val expectedResult = mockk<SendView>()
            coEvery {
                clientVault.sends().decrypt(
                    send = mockSend,
                )
            } returns expectedResult
            val result = vaultSdkSource.decryptSendList(
                sendList = listOf(mockSend),
            )
            assertEquals(
                listOf(expectedResult).asSuccess(),
                result,
            )
            coVerify {
                clientVault.sends().decrypt(
                    send = mockSend,
                )
            }
        }

    @Test
    fun `decryptSend should call SDK and return correct data wrapped in a Result`() =
        runBlocking {
            val mockSend = mockk<Send>()
            val expectedResult = mockk<SendView>()
            coEvery {
                clientVault.sends().decrypt(
                    send = mockSend,
                )
            } returns expectedResult
            val result = vaultSdkSource.decryptSend(
                send = mockSend,
            )
            assertEquals(
                expectedResult.asSuccess(), result,
            )
            coVerify {
                clientVault.sends().decrypt(
                    send = mockSend,
                )
            }
        }

    @Test
    fun `Folder decrypt should call SDK and return a Result with correct data`() = runBlocking {
        val mockFolder = mockk<Folder>()
        val expectedResult = mockk<FolderView>()
        coEvery {
            clientVault.folders().decrypt(
                folder = mockFolder,
            )
        } returns expectedResult
        val result = vaultSdkSource.decryptFolder(
            folder = mockFolder,
        )
        assertEquals(
            expectedResult.asSuccess(),
            result,
        )
        coVerify {
            clientVault.folders().decrypt(
                folder = mockFolder,
            )
        }
    }

    @Test
    fun `Folder decryptList should call SDK and return a Result with correct data`() = runBlocking {
        val mockFolders = mockk<List<Folder>>()
        val expectedResult = mockk<List<FolderView>>()
        coEvery {
            clientVault.folders().decryptList(
                folders = mockFolders,
            )
        } returns expectedResult
        val result = vaultSdkSource.decryptFolderList(
            folderList = mockFolders,
        )
        assertEquals(
            expectedResult.asSuccess(),
            result,
        )
        coVerify {
            clientVault.folders().decryptList(
                folders = mockFolders,
            )
        }
    }
}
