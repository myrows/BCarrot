package com.example.bcarrot.ui.vehicle

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.airbnb.lottie.LottieAnimationView
import com.example.bcarrot.R
import com.example.bcarrot.common.SharedPreferencesManager
import com.example.bcarrot.model.Vehicle
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_vehicle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class VehicleFragment : Fragment() {
    lateinit var edTextVehicle : EditText
    lateinit var edTextColour : EditText
    lateinit var edTextMatricula : EditText
    lateinit var buttonSave : Button
    lateinit var db: FirebaseFirestore
    lateinit var userIdDocument : String
    lateinit var lottieSuccess : LottieAnimationView
    companion object {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = FirebaseFirestore.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = inflater.inflate(R.layout.fragment_vehicle, container, false)
        edTextVehicle = view?.findViewById(R.id.editTextVehicle)!!
        edTextColour = view?.findViewById(R.id.editTextColour)!!
        edTextMatricula = view?.findViewById(R.id.editTextPlate)!!
        buttonSave = view.findViewById(R.id.buttonSaveCar)
        lottieSuccess = view.findViewById(R.id.lottieAnimationSuccess)
        // Get user vehicle model, colour and plate
        setVehicleData()
        //
        buttonSave.setOnClickListener {
            var vehicle : Vehicle = Vehicle( edTextVehicle.text.toString(), edTextColour.text.toString(), edTextMatricula.text.toString() )
            db.collection("users")
                .whereEqualTo("email", SharedPreferencesManager.getSomeStringValue("user").toString())
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        Log.d("Query", "${document.id} => ${document.data}")
                            userIdDocument = document.id
                            db.collection("users").document(userIdDocument).collection("vehicle")
                                .get()
                                .addOnSuccessListener { documents ->
                                    Log.d("QuerySize", "${documents.size()}")
                                        if (documents.size() == 0 || documents.isEmpty) {
                                            // Create vehicle
                                            addVehicle(vehicle)
                                        } else {
                                            updateVehicle()
                                        }

                                }
                                .addOnFailureListener { exception ->
                                    Log.w("Query", "Error getting documents: ", exception)
                                }
                    }

                }
                .addOnFailureListener { exception ->
                    Log.w("Query", "Error getting documents: ", exception)
                    Toast.makeText(context, "Error al crear el vehículo", Toast.LENGTH_LONG).show()
                }
        }
        // Inflate the layout for this fragment
        return view
    }

    fun addVehicle(vehicle : Vehicle) {
        db.collection("users").document(userIdDocument)
            .collection("vehicle").document().set(vehicle)
            .addOnSuccessListener {
                Log.d("Firestore", "Vehiculo añadido con éxito")
                lottieSuccess.visibility = View.VISIBLE
                lottieAnimationSuccess.playAnimation()
                GlobalScope.launch(context = Dispatchers.Main) {
                    delay( lottieSuccess.duration + 1000 )
                    lottieSuccess.cancelAnimation()
                    lottieSuccess.visibility = View.INVISIBLE
                }
            }
    }

    fun updateVehicle () {
        db.collection("users").document(userIdDocument)
            .collection("vehicle")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    Log.d("QueryYYUpdate", "${document.id} => ${document.data}")

                    var map = mutableMapOf<String, Any>()
                    map.put(
                        key = "model",
                        value = edTextVehicle.text.toString()
                    )
                    map.put(
                        key = "colour",
                        value = edTextColour.text.toString()
                    )
                    map.put(
                        key = "plate",
                        value = edTextMatricula.text.toString()
                    )
                    db.collection("users")
                        .document(userIdDocument).collection("vehicle").document(document.id).update(map)
                        .addOnSuccessListener {
                            lottieSuccess.visibility = View.VISIBLE
                            lottieAnimationSuccess.playAnimation()
                            GlobalScope.launch(context = Dispatchers.Main) {
                                delay( lottieSuccess.duration + 1000 )
                                lottieSuccess.cancelAnimation()
                                lottieSuccess.visibility = View.INVISIBLE
                            }
                        }.addOnFailureListener {
                            Log.d(
                                "Update",
                                "Ha ocurrido un error al actualizar el documento"
                            )
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.w(
                    "Query",
                    "Error getting documents: ",
                    exception
                )
            }
    }

    fun setVehicleData() {
        db.collection("users")
            .whereEqualTo("email", SharedPreferencesManager.getSomeStringValue("user").toString())
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    db.collection("users").document(document.id)
                        .collection("vehicle")
                        .get()
                        .addOnSuccessListener { documents ->
                            for ( document in documents ) {
                                var data : MutableMap<String, Any> = document.data
                                edTextVehicle.setText(data.getValue("model") as String)
                                edTextColour.setText(data.getValue("colour") as String)
                                edTextMatricula.setText(data.getValue("plate") as String)
                            }
                        }.addOnFailureListener { exception ->
                            Log.w(
                                "Query",
                                "Error getting documents: ",
                                exception
                            )
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.w(
                    "Query",
                    "Error getting documents: ",
                    exception
                )
            }
    }
}