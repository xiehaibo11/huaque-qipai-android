package com.nanbeiyule.game;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.InputFilter;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.nanbeiyule.game.wechat.WechatAuthResponse;
import com.nanbeiyule.game.wechat.WechatAuthStateStore;
import com.nanbeiyule.game.wechat.WechatCallbackContract;
import com.nanbeiyule.game.wechat.WechatLoginManager;
import java.io.File;
import java.util.List;

abstract class MainActivityRegionFlow extends MainActivityStartupFlow {
    protected void showChooseAreaPage() {
        if (isFinishing() || regionApiClient == null) {
            return;
        }
        if (regionCatalog != null) {
            displayChooseAreaPage();
            return;
        }
        openRegionWhenCatalogLoads = true;
        if (regionCatalogLoading) {
            Toast.makeText(
                            this,
                            R.string.choose_area_loading,
                            Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        loadRegionCatalog(true);
    }

    protected void preloadRegionCatalog() {
        if (isFinishing()
                || regionApiClient == null
                || regionCatalog != null
                || regionCatalogLoading) {
            return;
        }
        loadRegionCatalog(false);
    }

    protected void loadRegionCatalog(boolean showLoadingMessage) {
        regionCatalogLoading = true;
        if (showLoadingMessage) {
            Toast.makeText(
                            this,
                            R.string.choose_area_loading,
                            Toast.LENGTH_SHORT)
                    .show();
        }
        regionApiClient.loadCatalog(
                new RegionApiClient.Callback<>() {
                    @Override
                    public void onSuccess(RegionApiClient.Catalog result) {
                        regionCatalogLoading = false;
                        if (isFinishing()) {
                            return;
                        }
                        regionCatalog = result;
                        if (openRegionWhenCatalogLoads) {
                            openRegionWhenCatalogLoads = false;
                            displayChooseAreaPage();
                        } else {
                            updateLoginRegionNameIfVisible();
                        }
                    }

                    @Override
                    public void onError(String message) {
                        regionCatalogLoading = false;
                        boolean showError = openRegionWhenCatalogLoads;
                        openRegionWhenCatalogLoads = false;
                        if (!isFinishing() && showError) {
                            Toast.makeText(
                                            MainActivityRegionFlow.this,
                                            message,
                                            Toast.LENGTH_LONG)
                                    .show();
                        }
                    }
                });
    }

    protected void updateLoginRegionNameIfVisible() {
        if (!(loadingView instanceof LoginPageView)) {
            return;
        }
        String selectedAreaName = selectedAreaName();
        if (selectedAreaName != null) {
            LoginView loginView =
                    ((LoginPageView) loadingView).loginView();
            loginView.setSelectedRegionName(selectedAreaName);
        }
    }

    protected void displayChooseAreaPage() {
        if (regionCatalog == null || regionSelectionStore == null) {
            return;
        }
        long selectedLobbyId = regionSelectionStore.getSelectedLobbyId();
        if (regionCatalog.findLobby(selectedLobbyId) == null) {
            selectedLobbyId = regionCatalog.defaultLobbyId();
            regionSelectionStore.setSelectedLobbyId(selectedLobbyId);
        }
        ChooseAreaView chooseAreaView =
                new ChooseAreaView(this, regionCatalog, selectedLobbyId);
        if (originalLobbyAudioController != null) {
            chooseAreaView.setButtonClickSound(originalLobbyAudioController::playButtonClick);
        }
        chooseAreaView.setOnBackRequestedListener(
                new ChooseAreaView.OnBackRequestedListener() {
                    @Override
                    public void onBackRequested() {
                        if (returnToHomeAfterRegionSelection
                                && authSessionCoordinator != null
                                && authSessionCoordinator.hasRecoverableSession()) {
                            loadGameHome();
                        } else {
                            showLoginPage();
                        }
                    }
                });
        chooseAreaView.setOnRegionSelectedListener(
                new ChooseAreaView.OnRegionSelectedListener() {
                    @Override
                    public void onRegionSelected(RegionApiClient.Lobby lobby) {
                        onRegionSelectedFromMap(lobby);
                    }
                });
        chooseAreaView.setLayoutParams(
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
        loadingView = chooseAreaView;
        setContentView(chooseAreaView);
    }

    protected void onRegionSelectedFromMap(RegionApiClient.Lobby lobby) {
        if (regionSelectionStore == null) {
            return;
        }
        regionSelectionStore.setSelectedLobbyId(lobby.lobbyId());
        if (returnToHomeAfterRegionSelection
                && authSessionCoordinator != null
                && authSessionCoordinator.hasRecoverableSession()) {
            syncSelectedRegion(lobby.lobbyId(), this::loadGameHome);
        } else {
            syncSelectedRegion(lobby.lobbyId());
            showLoginPage();
        }
    }

    protected void syncSelectedRegion(long lobbyId) {
        syncSelectedRegion(lobbyId, null, null);
    }

    protected void syncSelectedRegion(long lobbyId, Runnable completion) {
        syncSelectedRegion(lobbyId, completion, null);
    }

    protected void syncSelectedRegion(
            long lobbyId,
            Runnable completion,
            Runnable failure) {
        if (lobbyId <= 0L
                || regionApiClient == null
                || authSessionCoordinator == null
                || !authSessionCoordinator.hasRecoverableSession()) {
            runCompletion(completion);
            return;
        }
        authSessionCoordinator.execute(
                (accessToken, callback) ->
                        regionApiClient.saveSelection(
                                lobbyId,
                                accessToken,
                                new RegionApiClient.Callback<>() {
                                    @Override
                                    public void onSuccess(Long result) {
                                        callback.onSuccess(result);
                                    }

                                    @Override
                                    public void onUnauthorized() {
                                        callback.onUnauthorized();
                                    }

                                    @Override
                                    public void onError(String message) {
                                        callback.onError(message);
                                    }
                                }),
                new AuthSessionCoordinator.Callback<Long>() {
                    @Override
                    public void onSuccess(Long ignored) {
                        runCompletion(completion);
                    }

                    @Override
                    public void onLoginRequired() {
                        runCompletion(failure);
                        if (!isFinishing()) {
                            showLoginPage();
                        }
                    }

                    @Override
                    public void onError(String message) {
                        if (!isFinishing()) {
                            Toast.makeText(
                                            MainActivityRegionFlow.this,
                                            message,
                                            Toast.LENGTH_SHORT)
                                    .show();
                        }
                        runCompletion(failure);
                    }
                });
    }

    protected void runCompletion(Runnable completion) {
        if (completion != null && !isFinishing()) {
            completion.run();
        }
    }

    protected String selectedAreaName() {
        if (regionCatalog == null || regionSelectionStore == null) {
            return null;
        }
        long selectedLobbyId = regionSelectionStore.getSelectedLobbyId();
        RegionApiClient.Lobby lobby = regionCatalog.findLobby(selectedLobbyId);
        return lobby == null ? null : lobby.areaName();
    }
}
