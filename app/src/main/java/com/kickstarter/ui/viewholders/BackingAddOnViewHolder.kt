package com.kickstarter.ui.viewholders

import android.util.Pair
import android.view.View
import androidx.annotation.NonNull
import androidx.recyclerview.widget.LinearLayoutManager
import com.kickstarter.R
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.RewardUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.models.Reward
import com.kickstarter.ui.adapters.RewardItemsAdapter
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.viewmodels.BackingAddOnViewHolderViewModel
import kotlinx.android.synthetic.main.add_on_items.view.*
import kotlinx.android.synthetic.main.add_on_title.view.*
import kotlinx.android.synthetic.main.item_add_on_pledge.view.*

class BackingAddOnViewHolder(private val view: View, viewListener: ViewListener) : KSViewHolder(view) {

    interface ViewListener {
        fun quantityPerId(quantityPerId: Pair<Int, Long>)
    }

    private var viewModel = BackingAddOnViewHolderViewModel.ViewModel(environment())
    private val ksString = environment().ksString()
    private val viewListener = viewListener

    init {

        val rewardItemAdapter = setUpItemAdapter()

        setListenerForDecreaseButton()
        setListenerForIncreaseButton()
        setListenerForAddButton()

        this.viewModel.outputs.description()
                .compose(bindToLifecycle())
                .subscribe {
                    this.view.add_on_description.text = it
                }

        this.viewModel.outputs.rewardItemsAreGone()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe {
                    ViewUtils.setGone(this.view.items_container, it)
                    ViewUtils.setGone(this.view.divider, it)
                }

        this.viewModel.outputs.rewardItems()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { rewardItemAdapter.rewardsItems(it) }

        this.viewModel.outputs.titleForAddOn()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { this.view.add_on_title_text_view.text = it }


        this.viewModel.outputs.description()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { this.view.add_on_description.text = it }


        this.viewModel.outputs.minimum()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe {
                    this.view.add_on_minimum.text = "$it "
                }

        this.viewModel.outputs.conversionIsGone()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { ViewUtils.setGone(this.view.add_on_conversion, it) }


        this.viewModel.outputs.conversion()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe {
                    this.view.add_on_conversion.text = this.ksString.format(context().getString(R.string.About_reward_amount), "reward_amount", it.toString())
                }


        this.viewModel.outputs.backerLimitPillIsGone()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { ViewUtils.setGone(this.view.addon_backer_limit, it) }

        this.viewModel.outputs.remainingQuantityPillIsGone()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { ViewUtils.setGone(this.view.addon_quantity_remaining, it) }

        this.viewModel.outputs.backerLimit()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe {
                    this.view.addon_backer_limit.text = this.ksString.format(context().getString(R.string.limit_limit_per_backer), "limit_per_backer", it)
                }

        this.viewModel.outputs.remainingQuantity()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe {
                    this.view.addon_quantity_remaining.text =
                            this.ksString.format(context().getString(R.string.rewards_info_time_left), "time", it)
                }

        this.viewModel.outputs.deadlineCountdownIsGone()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe {
                    ViewUtils.setGone(this.view.addon_time_left, it) }

        this.viewModel.outputs.deadlineCountdown()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { this.view.addon_time_left.text = formattedExpirationString(it)  }

        this.viewModel.outputs.shippingAmountIsGone()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe {
                    ViewUtils.setGone(this.view.add_on_shipping_amount, it)
                }

        this.viewModel.outputs.shippingAmount()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe {
                    if  (it.isNotEmpty()) {
                        val rewardAndShippingString = context().getString(R.string.reward_amount_plus_shipping_cost_each)
                        val stringSections = rewardAndShippingString.split("+")
                        val shippingString = "+" + stringSections[1]
                        this.view.add_on_shipping_amount.text = this.ksString.format(shippingString, "shipping_cost", it)
                    }
                }

        this.viewModel.outputs.addButtonIsGone()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { addButtonIsGone ->
                    if (addButtonIsGone) {
                        showStepper()
                    }
                    else {
                        hideStepper()
                    }
                }

        this.viewModel.outputs.quantityPerId()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { quantityPerId ->
                    quantityPerId?.let { this.viewListener.quantityPerId(it) }
                    val quantity = quantityPerId.first
                    this.view.decrease_quantity_add_on.isEnabled = (quantity != 0)
                    this.view.quantity_add_on.text = quantity.toString()
                }

        this.viewModel.outputs.disableIncreaseButton()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe {
                    this.view.increase_quantity_add_on.isEnabled = !it
                }
    }

    private fun hideStepper() {
        this.view.initial_state_add_on.visibility = View.VISIBLE
        this.view.stepper_container_add_on.visibility = View.GONE
        this.view.initial_state_add_on.isEnabled = true
        this.view.increase_quantity_add_on.isEnabled = false
    }

    private fun showStepper() {
        this.view.initial_state_add_on.visibility = View.GONE
        this.view.stepper_container_add_on.visibility = View.VISIBLE
        this.view.initial_state_add_on.isEnabled = false
        this.view.increase_quantity_add_on.isEnabled = true
    }

    private fun setListenerForAddButton() {
        this.view.initial_state_add_on.setOnClickListener {
            this.viewModel.inputs.addButtonPressed()
        }
    }

    private fun setListenerForIncreaseButton() {
        this.view.increase_quantity_add_on.setOnClickListener {
            this.viewModel.inputs.increaseButtonPressed()
        }
    }

    private fun setListenerForDecreaseButton() {
        this.view.decrease_quantity_add_on.setOnClickListener {
            this.viewModel.inputs.decreaseButtonPressed()
        }
    }

    private fun formattedExpirationString(@NonNull reward: Reward): String {
        val detail = RewardUtils.deadlineCountdownDetail(reward, context(), this.ksString)
        val value = RewardUtils.deadlineCountdownValue(reward)
        return "$value $detail"
    }

    override fun bindData(data: Any?) {
        if (data is (Pair<*, *>)) {
            if (data.second is Reward) {
                bindAddonsList(data as Pair<ProjectData, Reward>)
            }
        }
    }

    private fun bindAddonsList(projectDataAndAddOn: Pair<ProjectData, Reward>) {
        this.viewModel.inputs.configureWith(projectDataAndAddOn)
    }

    private fun setUpItemAdapter(): RewardItemsAdapter {
        val rewardItemAdapter = RewardItemsAdapter()
        val itemRecyclerView = view.add_on_item_recycler_view
        itemRecyclerView.adapter = rewardItemAdapter
        itemRecyclerView.layoutManager = LinearLayoutManager(context())
        return rewardItemAdapter
    }
}