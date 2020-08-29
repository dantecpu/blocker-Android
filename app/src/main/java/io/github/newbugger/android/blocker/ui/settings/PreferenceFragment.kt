package io.github.newbugger.android.blocker.ui.settings

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import io.github.newbugger.android.blocker.R
import io.github.newbugger.android.blocker.util.BuildUtil
import io.github.newbugger.android.blocker.util.ToastUtil
import io.github.newbugger.android.blocker.util.storage.DocumentFileLocalUtil
import io.github.newbugger.android.blocker.util.storage.SAFLocalUtil
import io.github.newbugger.android.libkit.utils.ConstantUtil


class PreferenceFragment: PreferenceFragmentCompat(),
        SettingsContract.SettingsView,
        Preference.OnPreferenceClickListener {

    private lateinit var presenter: SettingsPresenter

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var exportRulePreference: Preference
    private lateinit var importRulePreference: Preference
    private lateinit var exportIfwRulePreference: Preference
    private lateinit var importIfwRulePreference: Preference
    private lateinit var resetIfwPreference: Preference
    private lateinit var safPreference: Preference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        presenter = SettingsPresenter(requireContext(), this)
        sharedPreferences = preferenceManager.sharedPreferences
        findPreference()
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            startActivity(Intent(requireActivity(), SettingsActivity::class.java))
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    @RequiresApi(26)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && data != null) {
            when (requestCode) {
                ConstantUtil.documentRequestCode -> {
                    val uri = (data.data ?: return).also {
                        if (BuildUtil.BuildProperty.isBuildDebug()) Log.e(javaClass.name, it.toString())
                    }
                    DocumentFileLocalUtil.getDirectoryName(requireContext(), uri).let {
                        // needs to grant permission for each uri, even if it is of sub-directory
                        /*if (it == ConstantUtil.NAME_APP_NAME_DEFAULT) {
                            SAFLocalUtil.takePersistableUriPermission(requireActivity(), it, uri)
                        } else {
                            SAFLocalUtil.takePersistableUriPermission(requireContext(), it, uri)
                        }*/
                        SAFLocalUtil.takePersistableUriPermission(requireActivity(), it, uri)
                    }
                }
                else -> return
            }
        }
    }

    override fun showExportResult(isSucceed: Boolean, successfulCount: Int, failedCount: Int) {

    }

    override fun showImportResult(isSucceed: Boolean, successfulCount: Int, failedCount: Int) {

    }

    override fun showResetResult(isSucceed: Boolean) {

    }

    override fun showMessage(res: Int) {
        ToastUtil.showToast(res, Toast.LENGTH_SHORT)
    }

    @SuppressLint("CheckResult")
    override fun showDialog(title: String, message: String, action: () -> Unit) {
        showConfirmationDialog(title, message, action)
    }

    override fun showDialog(
        title: String,
        message: String,
        file: String?,
        action: (file: String?) -> Unit
    ) {
        requireActivity().let {
            AlertDialog.Builder(it)
                    .setTitle(title)
                    .setMessage(message)
                    .setCancelable(true)
                    .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
                    .setPositiveButton(R.string.ok) { _, _ -> action(file) }
                    .create()
                    .show()
        }
    }

    override fun onPreferenceClick(preference: Preference?): Boolean {
        if (preference == null) {
            return false
        }
        when (preference) {
            exportRulePreference -> showDialog(
                getString(R.string.warning),
                getString(R.string.export_all_rules_warning_message)
            ) {
                presenter.exportAllRules()
            }
            importRulePreference -> showDialog(
                getString(R.string.warning),
                getString(R.string.import_all_rules_warning_message)
            ) {
                presenter.importAllRules()
            }
            exportIfwRulePreference -> showDialog(
                getString(R.string.warning),
                getString(R.string.export_all_ifw_rules_warning_message)
            ) {
                presenter.exportAllIfwRules()
            }

            importIfwRulePreference -> showDialog(
                getString(R.string.warning),
                getString(R.string.import_all_ifw_rules_warning_message)
            ) {
                presenter.importAllIfwRules()
            }
            safPreference -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    showSAFIntent()
                }
            }
            /* resetIfwPreference -> showDialog(
                getString(R.string.warning),
                getString(R.string.reset_ifw_warning_message)
            ) {
                presenter.resetIFW()
            } */
            else -> return false
        }
        return true
    }

    @RequiresApi(26)
    private fun showSAFIntent() {
        val intent = SAFLocalUtil.intentActionOpenDocumentTree()
        startActivityForResult(intent, ConstantUtil.documentRequestCode)
    }

    private fun findPreference() {
        exportRulePreference = findPreference(getString(R.string.key_pref_export_rules))!!
        importRulePreference = findPreference(getString(R.string.key_pref_import_rules))!!
        importIfwRulePreference = findPreference(getString(R.string.key_pref_import_ifw_rules))!!
        exportIfwRulePreference = findPreference(getString(R.string.key_pref_export_ifw_rules))!!
        resetIfwPreference = findPreference(getString(R.string.key_pref_reset_ifw_rules))!!
        safPreference = findPreference(getString(R.string.pref_saf_key))!!
        exportRulePreference.onPreferenceClickListener = this
        importRulePreference.onPreferenceClickListener = this
        exportIfwRulePreference.onPreferenceClickListener = this
        importIfwRulePreference.onPreferenceClickListener = this
        resetIfwPreference.onPreferenceClickListener = this
        safPreference.onPreferenceClickListener = this
    }

    private fun showConfirmationDialog(
        title: String,
        message: String,
        action: () -> Unit
    ) {
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setCancelable(true)
            .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .setPositiveButton(R.string.ok) { _, _ -> action() }
            .create()
            .show()
    }

}
