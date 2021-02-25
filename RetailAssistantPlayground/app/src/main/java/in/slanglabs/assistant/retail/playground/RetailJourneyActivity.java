package in.slanglabs.assistant.retail.playground;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import in.slanglabs.assistants.retail.AssistantError;
import in.slanglabs.assistants.retail.Item;
import in.slanglabs.assistants.retail.NavigationInfo;
import in.slanglabs.assistants.retail.NavigationUserJourney;
import in.slanglabs.assistants.retail.OrderInfo;
import in.slanglabs.assistants.retail.OrderManagementUserJourney;
import in.slanglabs.assistants.retail.RetailUserJourney;
import in.slanglabs.assistants.retail.SearchInfo;
import in.slanglabs.assistants.retail.SearchUserJourney;
import in.slanglabs.assistants.retail.SlangRetailAssistant;

public class RetailJourneyActivity extends AppCompatActivity {
    private static final String TAG = "RetailPlayground";
    private boolean mIsVisible = true;
    private TextView mResult;
    private static AlertDialog sCancelConfirmationDialog, sSelectOrderDialog;
    private Button mSearchJourney, mOrderJourney;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.retail_journeys);

        mIsVisible = true;
        mSearchJourney = findViewById(R.id.search_journey);
        mOrderJourney = findViewById(R.id.order_journey);

        //Using API to toggle assistant visibility.
        Button toggle = findViewById(R.id.toggle_btn);
        toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsVisible) {
                    SlangRetailAssistant.getUI().hideTrigger(RetailJourneyActivity.this);
                    mIsVisible = false;
                } else {
                    SlangRetailAssistant.getUI().showTrigger(RetailJourneyActivity.this);
                    mIsVisible = true;
                }
            }
        });

        mResult = findViewById(R.id.action_results);
        mResult.setText(R.string.action_results_default_text);
        mResult.setMovementMethod(new ScrollingMovementMethod());

        registerAssistantAction();

        mSearchJourney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SlangRetailAssistant.startConversation(RetailUserJourney.SEARCH, RetailJourneyActivity.this);
            }
        });

        mOrderJourney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SlangRetailAssistant.startConversation(RetailUserJourney.ORDER_MANAGEMENT, RetailJourneyActivity.this);
            }
        });

        SlangRetailAssistant.getUI().showTrigger(this);
    }

    private void registerAssistantAction() {
        //Register user journey action handlers.
        SlangRetailAssistant.setAction(new SlangRetailAssistant.Action() {
            @Override
            public SearchUserJourney.AppState onSearch(SearchInfo searchInfo, SearchUserJourney searchJourney) {
                Log.e(TAG, "onSearch:" + searchInfo);
                showSearchJourneyOptions(searchInfo, searchJourney);
                return SearchUserJourney.AppState.WAITING;
            }

            @Override
            public OrderManagementUserJourney.AppState onOrderManagement(final OrderInfo orderInfo, final OrderManagementUserJourney orderManagementJourney) {
                Log.e(TAG, "onOrderManagement:" + orderInfo);
                showOrderJourneyOptions(orderInfo, orderManagementJourney);
                return OrderManagementUserJourney.AppState.WAITING;
            }

            @Override
            public NavigationUserJourney.AppState onNavigation(NavigationInfo navigationInfo, NavigationUserJourney navigationUserJourney) {
                Log.e(TAG, "onNavigation:" + navigationInfo);
                return NavigationUserJourney.AppState.UNSUPPORTED;
            }

            @Override
            public void onAssistantError(final AssistantError error) {
                Log.e(TAG, "onAssistantError:" + error.getDescription());
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(
                                RetailJourneyActivity.this,
                                "Error:" + error.getDescription(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
            }
        });
    }


    //App action handlers follows
    private void showSearchJourneyOptions(final SearchInfo searchInfo, final SearchUserJourney searchJourney) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View customLayout = getLayoutInflater().inflate(R.layout.search_journey, null);
        builder.setView(customLayout);
        final AlertDialog dialog = builder.create();

        //Show the details collected so far at the top
        TextView searchInfoView = customLayout.findViewById(R.id.search_info);
        searchInfoView.setText(searchInfo.toString());

        //Set the default app state based on assistant's knowledge.
        RadioGroup appStatesGroup = customLayout.findViewById(R.id.search_appstates);
        if (searchInfo.isAddToCart()) appStatesGroup.check(R.id.add_to_cart);

        //UNSUPPORTED
        Button unsupportedButton = customLayout.findViewById(R.id.unsupported);
        unsupportedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notifySearchAppState(searchJourney, SearchUserJourney.AppState.UNSUPPORTED);
                dialog.dismiss();
            }
        });

        //SUCCESS
        Button searchSuccessButton = customLayout.findViewById(R.id.success);
        searchSuccessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchJourney.setSuccess();
                notifySearchAppState(searchJourney, getSelectedSearchAppState(customLayout));
                dialog.dismiss();
            }
        });

        //FAILURE
        Button searchFailureGenericButton = customLayout.findViewById(R.id.failure_generic);
        searchFailureGenericButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchJourney.setFailure();
                notifySearchAppState(searchJourney, getSelectedSearchAppState(customLayout));
                dialog.dismiss();
            }
        });

        //ITEM NOT FOUND
        Button searchFailureItemNotFoundButton = customLayout.findViewById(R.id.failure_item_not_found);
        searchFailureItemNotFoundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchJourney.setItemNotFound();
                notifySearchAppState(searchJourney, getSelectedSearchAppState(customLayout));
                dialog.dismiss();
            }
        });

        //ITEM OUT OF STOCK
        Button searchFailureItemOutOfStockButton = customLayout.findViewById(R.id.failure_item_out_of_stock);
        searchFailureItemOutOfStockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchJourney.setItemOutOfStock();
                notifySearchAppState(searchJourney, getSelectedSearchAppState(customLayout));
                dialog.dismiss();
            }
        });

        //QUANTITY REQUIRED
        final Button quantityRequiredButton = customLayout.findViewById(R.id.quantity_required);
        final CheckBox nonVoiceSelect = customLayout.findViewById(R.id.nonvoice_select);
        nonVoiceSelect.setChecked(false);
        quantityRequiredButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nonVoiceSelect.isChecked()) {
                    //User has selected an item from the screen and app wants assistant to gather quantity.
                    //app needs to set the item ID so assistant can return it when quantity is gathered so app can add the right item to the cart.
                    SearchUserJourney.getContext().setItemId(String.valueOf(new Random().nextInt(100)));
                }
                searchJourney.setNeedItemQuantity();
                notifySearchAppState(searchJourney, SearchUserJourney.AppState.ADD_TO_CART);
                dialog.dismiss();
            }
        });

        //ITEM DISAMBIGUATION
        final Button addToCartAmbiguousButton = customLayout.findViewById(R.id.add_to_cart_ambiguous);
        addToCartAmbiguousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    searchJourney.setNeedDisambiguation();
                    searchJourney.notifyAppState(SearchUserJourney.AppState.ADD_TO_CART);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
            }
        });

        appStatesGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                quantityRequiredButton.setEnabled(checkedId == R.id.add_to_cart);
                addToCartAmbiguousButton.setEnabled(checkedId == R.id.add_to_cart);
            }
        });

        Item.Quantity quantity = searchInfo.getItem().getQuantity();
        if (null != quantity && quantity.getAmount() > 0) {
            quantityRequiredButton.setEnabled(false);
        }

        if (appStatesGroup.getCheckedRadioButtonId() == R.id.search) {
            quantityRequiredButton.setEnabled(false);
            addToCartAmbiguousButton.setEnabled(false);
        }

        dialog.show();
    }

    private SearchUserJourney.AppState getSelectedSearchAppState(View view) {
        RadioGroup appStatesGroup = view.findViewById(R.id.search_appstates);
        final int selectedAppStateId = appStatesGroup.getCheckedRadioButtonId();
        if (selectedAppStateId == R.id.add_to_cart) return SearchUserJourney.AppState.ADD_TO_CART;
        else return SearchUserJourney.AppState.SEARCH_RESULTS; //Default
    }

    private void notifySearchAppState(SearchUserJourney searchUserJourney, SearchUserJourney.AppState appState) {
        if (null == searchUserJourney) return;
        try {
            searchUserJourney.notifyAppState(appState);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (null != searchUserJourney.getSearchInfo()) mResult.setText(searchUserJourney.getSearchInfo().toJSONString());
    }

    private void showOrderJourneyOptions(final OrderInfo orderInfo, final OrderManagementUserJourney orderJourney) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View customLayout = getLayoutInflater().inflate(R.layout.order_journey, null);
        builder.setView(customLayout);
        final AlertDialog dialog = builder.create();

        //Show the details collected so far at the top
        TextView orderInfoView = customLayout.findViewById(R.id.order_info);
        orderInfoView.setText(orderInfo.toString());

        //Set the default app state based on assistant's knowledge.
        RadioGroup appStatesGroup = customLayout.findViewById(R.id.order_appstates);
        if (orderInfo.getAction() == OrderInfo.Action.CANCEL) appStatesGroup.check(R.id.cancel);

        //UNSUPPORTED
        Button unsupportedButton = customLayout.findViewById(R.id.unsupported);
        unsupportedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notifyOrderAppState(orderJourney, OrderManagementUserJourney.AppState.UNSUPPORTED);
                dialog.dismiss();
            }
        });

        //SUCCESS
        Button searchSuccessButton = customLayout.findViewById(R.id.success);
        searchSuccessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orderJourney.setSuccess();
                notifyOrderAppState(orderJourney, getSelectedOrderAppState(customLayout));
                dialog.dismiss();
            }
        });

        //FAILURE
        Button searchFailureGenericButton = customLayout.findViewById(R.id.failure_generic);
        searchFailureGenericButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orderJourney.setFailure();
                notifyOrderAppState(orderJourney, getSelectedOrderAppState(customLayout));
                dialog.dismiss();
            }
        });

        //ORDER NOT FOUND
        Button orderNotFoundButton = customLayout.findViewById(R.id.order_not_found);
        orderNotFoundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orderJourney.setOrderNotFound(orderInfo.getIndex());
                notifyOrderAppState(orderJourney, getSelectedOrderAppState(customLayout));
                dialog.dismiss();
            }
        });

        //NO ORDER HISTORY
        Button noOrderHistory = customLayout.findViewById(R.id.no_order_history);
        noOrderHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orderJourney.setOrdersEmpty();
                notifyOrderAppState(orderJourney, getSelectedOrderAppState(customLayout));
                dialog.dismiss();
            }
        });

        //ORDER INDEX REQUIRED
        Button orderIndexRequired = customLayout.findViewById(R.id.order_index_required);
        orderIndexRequired.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orderJourney.setOrderIndexRequired();
                notifyOrderAppState(orderJourney, getSelectedOrderAppState(customLayout));
                simulateOrderSelection(orderJourney, getSelectedOrderAppState(customLayout));
                dialog.dismiss();
            }
        });

        //CANCEL ORDER CONFIRMED
        final Button orderCancelConfirmed = customLayout.findViewById(R.id.cancel_confirmed);
        orderCancelConfirmed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orderJourney.setUserConfirmedCancel();
                notifyOrderAppState(orderJourney, OrderManagementUserJourney.AppState.CANCEL_ORDER);
                dialog.dismiss();
            }
        });

        //CANCEL ORDER DENIED
        final Button orderCancelDenied = customLayout.findViewById(R.id.cancel_denied);
        orderCancelDenied.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orderJourney.setUserDeniedCancel();
                notifyOrderAppState(orderJourney, OrderManagementUserJourney.AppState.CANCEL_ORDER);
                dialog.dismiss();
            }
        });

        //CANCEL CONFIRMATION REQUIRED
        final Button orderCancelConfirmationRequired = customLayout.findViewById(R.id.need_confirmation);
        orderCancelConfirmationRequired.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orderJourney.setConfirmationRequired();
                notifyOrderAppState(orderJourney, OrderManagementUserJourney.AppState.CANCEL_ORDER);
                dialog.dismiss();
                simulateConfirmOrderCancellation(orderJourney);
            }
        });

        //FORCE UI SELECT TO CANCEL
        final Button orderCancelUISelect = customLayout.findViewById(R.id.force_ui_select);
        orderCancelUISelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orderJourney.setForceUISelectToCancel();
                notifyOrderAppState(orderJourney, OrderManagementUserJourney.AppState.CANCEL_ORDER);
                dialog.dismiss();
            }
        });

        appStatesGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                orderCancelConfirmed.setEnabled(checkedId == R.id.cancel);
                orderCancelDenied.setEnabled(checkedId == R.id.cancel);
                orderCancelConfirmationRequired.setEnabled(checkedId == R.id.cancel);
                orderCancelUISelect.setEnabled(checkedId == R.id.cancel);
            }
        });

        if (orderInfo.getAction() == OrderInfo.Action.VIEW ||
                orderInfo.getCancelConfirmationStatus() == OrderInfo.CancelConfirmationStatus.CONFIRMED) {
            orderCancelConfirmed.setEnabled(false);
            orderCancelDenied.setEnabled(false);
            orderCancelConfirmationRequired.setEnabled(false);
            orderCancelUISelect.setEnabled(false);
        }

        if (orderInfo.getIndex() != 0) {
            orderIndexRequired.setEnabled(false);
            orderCancelUISelect.setEnabled(false);
        }

        if (appStatesGroup.getCheckedRadioButtonId() == R.id.view) {
            orderCancelConfirmed.setEnabled(false);
            orderCancelDenied.setEnabled(false);
            orderCancelConfirmationRequired.setEnabled(false);
            orderCancelUISelect.setEnabled(false);
        }

        if (null != sCancelConfirmationDialog) sCancelConfirmationDialog.dismiss();
        if (null != sSelectOrderDialog) sSelectOrderDialog.dismiss();
        dialog.show();
    }

    private void notifyOrderAppState(OrderManagementUserJourney orderManagementUserJourney, OrderManagementUserJourney.AppState appState) {
        if (null == orderManagementUserJourney) return;
        try {
            orderManagementUserJourney.notifyAppState(appState);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (null != orderManagementUserJourney.getOrderInfo()) {
            mResult.setText(orderManagementUserJourney.getOrderInfo().toJSONString());
        }
    }

    private OrderManagementUserJourney.AppState getSelectedOrderAppState(View view) {
        RadioGroup appStatesGroup = view.findViewById(R.id.order_appstates);
        final int selectedAppStateId = appStatesGroup.getCheckedRadioButtonId();
        if (selectedAppStateId == R.id.cancel) return OrderManagementUserJourney.AppState.CANCEL_ORDER;
        else return OrderManagementUserJourney.AppState.VIEW_ORDER; //Default
    }

    private void simulateOrderSelection(final OrderManagementUserJourney orderJourney, final OrderManagementUserJourney.AppState appState) {
        sSelectOrderDialog = new AlertDialog.Builder(RetailJourneyActivity.this)
                .setTitle("Order History")
                .setMessage("Select order")
                .setNegativeButton("Last", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(RetailJourneyActivity.this, "'Last' order selected", Toast.LENGTH_LONG).show();
                        OrderManagementUserJourney.getContext().setOrderIndex(-1);
                        if (appState == OrderManagementUserJourney.AppState.VIEW_ORDER) orderJourney.setSuccess();
                        else orderJourney.setConfirmationRequired();
                        notifyOrderAppState(orderJourney, appState);
                    }
                })
                .setNeutralButton("First", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(RetailJourneyActivity.this, "'First' order selected", Toast.LENGTH_LONG).show();
                        OrderManagementUserJourney.getContext().setOrderIndex(1);
                        if (appState == OrderManagementUserJourney.AppState.VIEW_ORDER) orderJourney.setSuccess();
                        else orderJourney.setConfirmationRequired();
                        notifyOrderAppState(orderJourney, appState);
                    }
                })
                .setPositiveButton("Second", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(RetailJourneyActivity.this, "'Second' order selected", Toast.LENGTH_LONG).show();
                        OrderManagementUserJourney.getContext().setOrderIndex(2);
                        if (appState == OrderManagementUserJourney.AppState.VIEW_ORDER) orderJourney.setSuccess();
                        else orderJourney.setConfirmationRequired();
                        notifyOrderAppState(orderJourney, appState);
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .create();
        sSelectOrderDialog.show();
    }

    private void simulateConfirmOrderCancellation(final OrderManagementUserJourney orderJourney) {
        sCancelConfirmationDialog = new AlertDialog.Builder(RetailJourneyActivity.this)
                .setTitle("Order Cancellation")
                .setMessage("Do you really want to cancel this order?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        orderJourney.setUserConfirmedCancel();
                        notifyOrderAppState(orderJourney, OrderManagementUserJourney.AppState.CANCEL_ORDER);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        orderJourney.setUserDeniedCancel();
                        notifyOrderAppState(orderJourney, OrderManagementUserJourney.AppState.CANCEL_ORDER);
                    }
                })
                .create();
        sCancelConfirmationDialog.show();
    }
}
