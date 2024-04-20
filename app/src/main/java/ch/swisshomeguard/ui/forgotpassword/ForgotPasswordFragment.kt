package ch.swisshomeguard.ui.forgotpassword

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import ch.swisshomeguard.R
import ch.swisshomeguard.data.DefaultLoginRepository
import ch.swisshomeguard.data.LoginService
import ch.swisshomeguard.data.Result
import ch.swisshomeguard.ui.login.LoginViewModel
import ch.swisshomeguard.utils.EventObserver
import kotlinx.android.synthetic.main.fragment_forgot_password.*
import kotlinx.android.synthetic.main.login_fragment.editTextEmail

//forgot password screen
class ForgotPasswordFragment : Fragment() {
    private lateinit var viewModel: LoginViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_forgot_password, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val repository = DefaultLoginRepository(LoginService.create())
        viewModel = ViewModelProvider(
            this,
            LoginViewModel.Factory(repository)
        ).get(LoginViewModel::class.java)

        btnSubmit.setOnClickListener {
            viewModel.forgotPassword(
                email = editTextEmail.text.toString()
            )
        }

        viewModel.forgotPasswordResult.observe(viewLifecycleOwner, EventObserver {
            when (it) {
                is Result.Success -> {
                    progressBar.visibility = View.GONE
                    Toast.makeText(
                        this@ForgotPasswordFragment.activity,
                        this@ForgotPasswordFragment.getString(R.string.forgotPasswordSuccessMessage),
                        Toast.LENGTH_SHORT
                    ).show()
                    findNavController().navigate(R.id.action_navigation_forgotPasswordFragment_to_loginFragment)
                }
                is Result.Loading -> {
                    progressBar.visibility = View.VISIBLE
                }
                is Result.Error -> {
                    progressBar.visibility = View.GONE
                    Toast.makeText(
                        this@ForgotPasswordFragment.activity,
                        this@ForgotPasswordFragment.getString(R.string.forgotPasswordErrorMessage),
                        Toast.LENGTH_SHORT
                    ).show()

                }
            }
        })
    }
}