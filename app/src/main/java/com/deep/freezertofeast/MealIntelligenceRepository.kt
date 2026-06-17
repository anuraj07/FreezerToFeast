package com.deep.freezertofeast

import android.graphics.Bitmap
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import com.google.firebase.ai.type.content
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MealIntelligenceRepository {

    private val mealDataSchema = Schema.obj(
        properties = mapOf(
            "recipeName" to Schema.string("The name of the recipe"),
            "prepTime" to Schema.integer("The preparation + cooking time in minutes"),
            "ingredientsUsed" to Schema.array(
                Schema.string(),
                description = "List of ingredients used in the recipe including the identified main ingredient and any side ingredients suggested"
            ),
            "steps" to Schema.array(
                Schema.string(),
                description = "Step-by-step instructions to prepare the recipe"
            )
        )
    )

    private val generativeModel = Firebase.ai.generativeModel(
        modelName = "gemini-2.5-flash",
        systemInstruction = content {
            text("You are an experienced Indian home chef. Suggest a suitable recipe based on the user's available ingredients. Assume that staples like oil, ghee, atta (wheat flour), rice, and basic masalas (salt, turmeric, red chili powder, cumin, mustard seeds, coriander powder) are always available in the kitchen. You must output the recipe strictly in JSON format conforming to the requested schema.")
        },
        generationConfig = generationConfig {
            responseMimeType = "application/json"
            responseSchema = mealDataSchema
        }
    )

    fun generateAndLogMeal(rawInput: String, mealSlot: String): Flow<Resource<MealData>> = flow {
        emit(Resource.Loading)

        try {
            // 1. Resolve Auth User
            val currentUser = Firebase.auth.currentUser
                ?: throw IllegalStateException("User must be authenticated to generate and log meals.")
            val userId = currentUser.uid

            // 2. Call Gemini API client-side
            val response = generativeModel.generateContent(
                content {
                    text("Suggest a recipe using these ingredients: $rawInput")
                }
            )
            
            val jsonText = response.text 
                ?: throw IllegalStateException("Received empty response from the AI model.")

            // 3. Parse JSON response using Kotlinx Serialization
            val mealData = try {
                Json.decodeFromString<MealData>(jsonText)
            } catch (e: Exception) {
                throw IllegalArgumentException("Failed to parse JSON response from the model. Raw response: $jsonText", e)
            }

            // 4. Format currentDate (YYYY-MM-DD)
            val currentDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

            // 5. Build Firestore payload
            val mealDataMap = mapOf(
                "recipeName" to mealData.recipeName,
                "prepTime" to mealData.prepTime,
                "ingredientsUsed" to mealData.ingredientsUsed,
                "steps" to mealData.steps
            )

            val updatePayload = hashMapOf<String, Any>(
                mealSlot to mealDataMap,
                "timestamp" to FieldValue.serverTimestamp()
            )

            // 6. Write directly to Firestore: freezer_to_feast/app/users/{userId}/daily_meals/{YYYY-MM-DD}
            Firebase.firestore.collection("freezer_to_feast")
                .document("app")
                .collection("users")
                .document(userId)
                .collection("daily_meals")
                .document(currentDate)
                .set(updatePayload, SetOptions.merge())
                .await()

            // 7. Emit success
            emit(Resource.Success(mealData))

        } catch (e: IllegalStateException) {
            emit(Resource.Error("Authentication or state error: ${e.localizedMessage}", e))
        } catch (e: IllegalArgumentException) {
            emit(Resource.Error("Malformed response payload: ${e.localizedMessage}", e))
        } catch (e: java.io.IOException) {
            emit(Resource.Error("Network failure occurred: ${e.localizedMessage}", e))
        } catch (e: Exception) {
            emit(Resource.Error("An unexpected error occurred: ${e.localizedMessage}", e))
        }
    }

    fun generateAndLogMealWithImage(bitmap: Bitmap, mealSlot: String): Flow<Resource<MealData>> = flow {
        emit(Resource.Loading)

        try {
            // 1. Resolve Auth User
            val currentUser = Firebase.auth.currentUser
                ?: throw IllegalStateException("User must be authenticated to generate and log meals.")
            val userId = currentUser.uid

            // 2. Call Gemini API client-side with image and text instruction
            val response = generativeModel.generateContent(
                content {
                    image(bitmap)
                    text("Identify the main ingredient inside this photo of a freezer/fridge and suggest a suitable recipe using it. Also identify and list any side ingredients that would go well with it. Output in JSON format.")
                }
            )
            
            val jsonText = response.text 
                ?: throw IllegalStateException("Received empty response from the AI model.")

            // 3. Parse JSON response using Kotlinx Serialization
            val mealData = try {
                Json.decodeFromString<MealData>(jsonText)
            } catch (e: Exception) {
                throw IllegalArgumentException("Failed to parse JSON response from the model. Raw response: $jsonText", e)
            }

            // 4. Format currentDate (YYYY-MM-DD)
            val currentDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

            // 5. Build Firestore payload
            val mealDataMap = mapOf(
                "recipeName" to mealData.recipeName,
                "prepTime" to mealData.prepTime,
                "ingredientsUsed" to mealData.ingredientsUsed,
                "steps" to mealData.steps
            )

            val updatePayload = hashMapOf<String, Any>(
                mealSlot to mealDataMap,
                "timestamp" to FieldValue.serverTimestamp()
            )

            // 6. Write directly to Firestore: freezer_to_feast/app/users/{userId}/daily_meals/{YYYY-MM-DD}
            Firebase.firestore.collection("freezer_to_feast")
                .document("app")
                .collection("users")
                .document(userId)
                .collection("daily_meals")
                .document(currentDate)
                .set(updatePayload, SetOptions.merge())
                .await()

            // 7. Emit success
            emit(Resource.Success(mealData))

        } catch (e: IllegalStateException) {
            emit(Resource.Error("Authentication or state error: ${e.localizedMessage}", e))
        } catch (e: IllegalArgumentException) {
            emit(Resource.Error("Malformed response payload: ${e.localizedMessage}", e))
        } catch (e: java.io.IOException) {
            emit(Resource.Error("Network failure occurred: ${e.localizedMessage}", e))
        } catch (e: Exception) {
            emit(Resource.Error("An unexpected error occurred: ${e.localizedMessage}", e))
        }
    }
}
