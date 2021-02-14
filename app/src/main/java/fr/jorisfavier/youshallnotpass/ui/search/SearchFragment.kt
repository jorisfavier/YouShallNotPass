package fr.jorisfavier.youshallnotpass.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.android.support.AndroidSupportInjection
import fr.jorisfavier.youshallnotpass.R
import fr.jorisfavier.youshallnotpass.databinding.FragmentSearchBinding
import fr.jorisfavier.youshallnotpass.model.Item
import fr.jorisfavier.youshallnotpass.model.ItemDataType
import fr.jorisfavier.youshallnotpass.utils.toast
import jp.wasabeef.recyclerview.animators.FadeInRightAnimator
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

class SearchFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding: FragmentSearchBinding

    private val viewModel: SearchViewModel by viewModels { viewModelFactory }

    private val searchAdapter by lazy {
        SearchResultAdapter(
            this::navigateToEditItemFragment,
            this::deleteItem,
            viewModel::decryptPassword,
            this::copyToClipboard,
            viewModel::sendToDesktop
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        initSettings()
        binding.searchAddNewItemButton.setOnClickListener {
            navigateToCreateNewItem()
        }
        (activity as AppCompatActivity).supportActionBar?.hide()
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshItems()
    }

    private fun initSettings() {
        binding.searchSettingsButton.setOnClickListener {
            val action = SearchFragmentDirections.actionSearchFragmentToSettingsFragment()
            findNavController().navigate(action)
        }
    }

    private fun initRecyclerView() {
        binding.searchRecyclerview.apply {
            adapter = searchAdapter
            setHasFixedSize(true)
            itemAnimator = FadeInRightAnimator()
        }
        viewModel.results.observe(viewLifecycleOwner) { result ->
            searchAdapter.updateResults(result)
        }
    }

    private fun navigateToCreateNewItem() {
        val action = SearchFragmentDirections.actionSearchFragmentToItemFragment()
        findNavController().navigate(action)
    }

    private fun navigateToEditItemFragment(item: Item) {
        val action = SearchFragmentDirections.actionSearchFragmentToItemFragment(item.id)
        findNavController().navigate(action)
    }

    private fun deleteItem(item: Item) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_title)
            .setMessage(R.string.delete_confirmation)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                lifecycleScope.launchWhenStarted {
                    viewModel.deleteItem(item).collect {
                        var message = R.string.error_occurred
                        if (it.isSuccess) {
                            searchAdapter.removeItem(item)
                            message = R.string.delete_success
                        }
                        Toast.makeText(context, getString(message), Toast.LENGTH_LONG).show()
                    }
                }
            }
            .show()
    }

    private fun copyToClipboard(item: Item, type: ItemDataType) {
        viewModel.copyToClipboard(item, type)
            .onSuccess { context?.toast(it) }
            .onFailure { context?.toast(R.string.error_occurred) }
    }

    private fun copyToDesktop(item: Item, type: ItemDataType) {
        viewModel.sendToDesktop(item, type)
            .onSuccess { context?.toast(it) }
            .onFailure { context?.toast(R.string.error_occurred) }
    }
}
