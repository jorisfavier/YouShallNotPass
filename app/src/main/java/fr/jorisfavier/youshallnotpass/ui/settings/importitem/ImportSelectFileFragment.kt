package fr.jorisfavier.youshallnotpass.ui.settings.importitem

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.github.appintro.SlidePolicy
import dagger.android.support.AndroidSupportInjection
import fr.jorisfavier.youshallnotpass.R
import fr.jorisfavier.youshallnotpass.utils.toast
import kotlinx.android.synthetic.main.fragment_import_select_file.*
import javax.inject.Inject

class ImportSelectFileFragment : Fragment(R.layout.fragment_import_select_file), SlidePolicy {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    val viewModel: ImportItemViewModel by activityViewModels { viewModelFactory }

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            viewModel.setUri(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        importButton.setOnClickListener { getContent.launch("*/*") }
    }

    override val isPolicyRespected: Boolean
        get() = viewModel.isFileSelected

    override fun onUserIllegallyRequestedNextPage() {
        context?.toast(R.string.please_select_file)
    }

    companion object {
        fun newInstance(): ImportSelectFileFragment {
            return ImportSelectFileFragment()
        }
    }
}