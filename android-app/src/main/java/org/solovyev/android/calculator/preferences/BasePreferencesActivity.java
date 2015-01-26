package org.solovyev.android.calculator.preferences;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import org.solovyev.android.calculator.AdView;
import org.solovyev.android.calculator.CalculatorApplication;
import org.solovyev.android.calculator.R;
import org.solovyev.android.checkout.*;

import javax.annotation.Nonnull;

import static java.util.Arrays.asList;

public abstract class BasePreferencesActivity extends SherlockPreferenceActivity {

	private final ActivityCheckout checkout = Checkout.forActivity(this, CalculatorApplication.getInstance().getBilling(), Products.create().add(ProductTypes.IN_APP, asList("ad_free")));
	private Inventory inventory;
	private AdView adView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		checkout.start();
		inventory = checkout.loadInventory();
	}


	private class InventoryListener implements Inventory.Listener {
		@Override
		public void onLoaded(@Nonnull Inventory.Products products) {
			final Inventory.Product product = products.get(ProductTypes.IN_APP);
			final boolean adFree = product.isPurchased("ad_free");
			onShowAd(!adFree);
		}
	}

	protected void onShowAd(boolean show) {
		if (show) {
			if (adView != null) {
				return;
			}
			adView = (AdView) LayoutInflater.from(this).inflate(R.layout.ad, null);
			adView.show();
			getListView().addHeaderView(adView);
		} else {
			if (adView == null) {
				return;
			}
			getListView().removeHeaderView(adView);
			adView.hide();
			adView = null;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (checkout.onActivityResult(requestCode, resultCode, data)) {
			return;
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (adView != null) {
			adView.resume();
		}
		inventory.whenLoaded(new InventoryListener());
	}

	@Override
	protected void onPause() {
		if (adView != null) {
			adView.pause();
		}
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		if (adView != null) {
			adView.destroy();
		}
		checkout.stop();
		super.onDestroy();
	}
}