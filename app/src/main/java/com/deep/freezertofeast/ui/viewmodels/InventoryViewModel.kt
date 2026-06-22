package com.deep.freezertofeast.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deep.freezertofeast.PantryItem
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.net.URLEncoder

class InventoryViewModel : ViewModel() {
    private val _pantryItems = MutableStateFlow<List<PantryItem>>(emptyList())
    val pantryItems: StateFlow<List<PantryItem>> = _pantryItems.asStateFlow()

    private var listenerRegistration: ListenerRegistration? = null

    init {
        startListening()
    }

    private fun startListening() {
        val user = Firebase.auth.currentUser ?: return
        val db = Firebase.firestore
        
        listenerRegistration = db.collection("freezer_to_feast")
            .document("app")
            .collection("users")
            .document(user.uid)
            .collection("pantry_inventory")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val items = mutableListOf<PantryItem>()
                    for (doc in snapshot.documents) {
                        val id = doc.id
                        val name = doc.getString("name") ?: ""
                        val category = doc.getString("category") ?: "perishables"
                        val quantity = doc.getString("quantity") ?: ""
                        val expiryInfo = doc.getString("expiryInfo") ?: ""
                        val tag = doc.getString("tag") ?: ""
                        val outOfStock = doc.getBoolean("outOfStock") ?: false
                        val lastRestocked = doc.getString("lastRestocked") ?: ""
                        val imageUrl = doc.getString("imageUrl") ?: ""
                        
                        items.add(PantryItem(id, name, category, quantity, expiryInfo, tag, outOfStock, lastRestocked, imageUrl))
                    }
                    _pantryItems.value = items
                }
            }
    }

    fun addItem(
        name: String,
        category: String,
        quantity: String,
        expiryInfo: String,
        tag: String,
        outOfStock: Boolean = false
    ) {
        val user = Firebase.auth.currentUser ?: return
        val db = Firebase.firestore
        viewModelScope.launch {
            try {
                val querySafe = URLEncoder.encode(name, "UTF-8")
                val generatedImageUrl = "https://image.pollinations.ai/prompt/gourmet%20culinary%20photo%20of%20${querySafe}%20minimalist"
                
                val docData = hashMapOf(
                    "name" to name,
                    "category" to category,
                    "quantity" to quantity,
                    "expiryInfo" to expiryInfo,
                    "tag" to tag,
                    "outOfStock" to outOfStock,
                    "lastRestocked" to "Today",
                    "imageUrl" to generatedImageUrl,
                    "timestamp" to com.google.firebase.Timestamp.now()
                )
                
                db.collection("freezer_to_feast")
                    .document("app")
                    .collection("users")
                    .document(user.uid)
                    .collection("pantry_inventory")
                    .add(docData)
                    .await()
            } catch (e: Exception) {
                // Ignore failures in development
            }
        }
    }

    fun toggleOutOfStock(item: PantryItem) {
        val user = Firebase.auth.currentUser ?: return
        val db = Firebase.firestore
        viewModelScope.launch {
            try {
                db.collection("freezer_to_feast")
                    .document("app")
                    .collection("users")
                    .document(user.uid)
                    .collection("pantry_inventory")
                    .document(item.id)
                    .update("outOfStock", !item.outOfStock)
                    .await()
            } catch (e: Exception) {
                // Ignore updates failures in dev
            }
        }
    }

    fun deleteItem(itemId: String) {
        val user = Firebase.auth.currentUser ?: return
        val db = Firebase.firestore
        viewModelScope.launch {
            try {
                db.collection("freezer_to_feast")
                    .document("app")
                    .collection("users")
                    .document(user.uid)
                    .collection("pantry_inventory")
                    .document(itemId)
                    .delete()
                    .await()
            } catch (e: Exception) {
                // Ignore deletes failures in dev
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }
}
