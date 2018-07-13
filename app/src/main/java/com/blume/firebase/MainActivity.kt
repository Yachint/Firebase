package com.blume.firebase

import android.app.Activity
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*
import com.firebase.ui.auth.AuthUI
import java.util.*
import java.util.Arrays.asList
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import android.content.Intent
import android.support.design.widget.Snackbar
import android.support.v4.app.FragmentActivity
import android.util.Log


class MainActivity : AppCompatActivity() {

    val notes = ArrayList<String>()
    lateinit var arrayAdapter : ArrayAdapter<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //FirebaseApp.initializeApp(this)
        val RC_SIGN_IN = 123
        val dbRef = FirebaseDatabase.getInstance().getReference()

        arrayAdapter = ArrayAdapter(this,
                android.R.layout.simple_list_item_1,
                android.R.id.text1,
                notes)

        listView.adapter = arrayAdapter


        val firebaseUser = FirebaseAuth.getInstance().currentUser

        if(firebaseUser!=null)
        {
            //logged in
            addListners()

        }else{
            //logged out

            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(Arrays.asList(
                                    AuthUI.IdpConfig.GoogleBuilder().build(),
                                    AuthUI.IdpConfig.EmailBuilder().build(),
                                    AuthUI.IdpConfig.PhoneBuilder().build()))
                            .build(),
                    RC_SIGN_IN)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        // RC_SIGN_IN is the request code you passed into startActivityForResult(...) when starting the sign in flow.
        if (requestCode == 123) {
            val response = IdpResponse.fromResultIntent(data)

            // Successfully signed in
            if (resultCode == Activity.RESULT_OK) {
                addListners()
                Log.e("TAG","onLog in :"+ firebaseUser!!.displayName)
                Log.e("TAG","onLog in :"+ firebaseUser.uid)
            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    Snackbar.make(root_layout,"Sign-in Cancelled", Snackbar.LENGTH_SHORT).show()
                    return
                }

                if (response.error!!.errorCode == ErrorCodes.NO_NETWORK) {
                    Snackbar.make(root_layout,"No internet Connection", Snackbar.LENGTH_SHORT).show()
                    return
                }

                Snackbar.make(root_layout,"Unknown Error", Snackbar.LENGTH_SHORT).show()
                Log.e("TAG", "Sign-in error: ", response.error)
            }
        }
    }

    fun addListners()
    {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val dbRef = FirebaseDatabase.getInstance().getReference()
        btnSave.setOnClickListener {
            val note =  etNote.text.toString()

            //val n = Note("Hello","World")


            dbRef.child("note").child(firebaseUser!!.uid).push().setValue(note)
//            dbRef.child("todo").push().setValue(note)
        }

        dbRef.child("note").child(firebaseUser!!.uid).addChildEventListener(object : ChildEventListener{
            override fun onCancelled(p0: DatabaseError) {
                //When the read operation failed

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                //When position of a sub-node changes
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                //An Existing data is updated
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                //Called when a new data is inserted into the "note" node
                //Note data = p0.getValue(Note.class)
                val data  = p0.getValue(String::class.java)
                notes.add(data!!)
                arrayAdapter.notifyDataSetChanged()

            }

            override fun onChildRemoved(p0: DataSnapshot) {
                //When data is removed
            }
        })

        /*dbRef.child("note").addValueEventListener(object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    //As soon as the database changes, gives the whole database, regardless of the operation
                }
            })*/
    }
}
