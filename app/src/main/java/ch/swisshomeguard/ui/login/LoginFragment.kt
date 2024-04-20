package ch.swisshomeguard.ui.login

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import ch.swisshomeguard.R
import ch.swisshomeguard.data.DefaultLoginRepository
import ch.swisshomeguard.data.LoginService
import ch.swisshomeguard.data.Result
import ch.swisshomeguard.ui.home.HomeFragment
import ch.swisshomeguard.utils.EventObserver
import ch.swisshomeguard.utils.HomeguardTokenUtils
import ch.swisshomeguard.utils.SharedPreferencesUtil
import kotlinx.android.synthetic.main.loading_or_error.*
import kotlinx.android.synthetic.main.login_fragment.*
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class LoginFragment : Fragment() {

    private lateinit var viewModel: LoginViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.login_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val repository = DefaultLoginRepository(LoginService.create())
        viewModel = ViewModelProvider(
            this,
            LoginViewModel.Factory(repository)
        ).get(LoginViewModel::class.java)

        loginButton.setOnClickListener {
            viewModel.login(
                email = editTextEmail.text.toString(),
                password = editTextPassword.text.toString(),
                deviceLanguage = Locale.getDefault().language
            )
        }
        //forgot password screen redirection
        tvForgotPassword.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_loginFragment_to_forgotPasswordFragment)
        }

        viewModel.loginResult.observe(viewLifecycleOwner, EventObserver {
            when (it) {
                is Result.Success -> {
                    showDataViews()
                    HomeguardTokenUtils.saveHomeguardToken(it.data)
                    SharedPreferencesUtil.saveShouldReload(true)
                    findNavController().navigate(
                        LoginFragmentDirections.actionLoginFragmentToNavigationHome(
                            true
                        )
                    )
                }
                is Result.Loading -> {
                    showLoading()
                }
                is Result.Error -> {
                    showError(it)
                }
            }
        })
    }

    private fun showDataViews() {
        loadingOrErrorLayout.visibility = View.GONE
    }

    private fun showLoading() {
        loadingOrErrorLayout.visibility = View.VISIBLE
        progressBar.visibility = View.VISIBLE
        errorMessage.visibility = View.GONE
    }

    private fun showError(it: Result.Error) {
        loadingOrErrorLayout.visibility = View.GONE
        isNetworkAvailable(it.exception.message)
    }

    private fun isNetworkAvailable(errorMessage: String?) {
        HomeFragment.InternetCheck { internet ->
            if (!internet) Toast.makeText(
                activity,
                getString(R.string.noInternetConnectivity),
                Toast.LENGTH_SHORT
            ).show()
            else
                Toast.makeText(
                    activity,
                    getString(R.string.invalidCredentials),
                    Toast.LENGTH_SHORT
                ).show()
        }
    }
}