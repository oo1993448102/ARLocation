/*
 * Copyright 2018 Google LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.co.appoly.sceneform_example;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ar.core.Frame;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableException;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ViewRenderable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import uk.co.appoly.arcorelocation.LocationMarker;
import uk.co.appoly.arcorelocation.LocationScene;
import uk.co.appoly.arcorelocation.rendering.LocationNode;
import uk.co.appoly.arcorelocation.rendering.LocationNodeRender;
import uk.co.appoly.arcorelocation.utils.ARLocationPermissionHelper;

/**
 * This is a simple example that shows how to create an augmented reality (AR) application using the
 * ARCore and Sceneform APIs.
 */
public class LocationActivity extends AppCompatActivity {
    private boolean installRequested;
    private boolean hasFinishedLoading = false;

//    private Snackbar loadingMessageSnackbar = null;

    private ArSceneView arSceneView;

    // Renderables for this example
    private ModelRenderable andyRenderable;
    private ViewRenderable exampleLayoutRenderable;
    private ViewRenderable testLayoutRenderable;

    // Our ARCore-Location scene
    private LocationScene locationScene;
    private boolean first = true;

    private List<House> houseList = new ArrayList<>();
    private List<ViewRenderable> viewRenderables = new ArrayList<>();


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
    // CompletableFuture requires api level 24
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sceneform);
        arSceneView = findViewById(R.id.ar_scene_view);
        House house_1 = new House("歌林春天馨园", 31.27287039094972, 121.44389121570103);
        House house_2 = new House("金荣公寓", 31.273142774057952, 121.44511075546684);
        House house_3 = new House("宜川小区", 31.270879818488897, 121.4389931970126);
        House house_4 = new House("望景苑", 31.269698898381982, 121.43899980991299);
        House house_5 = new House("大宁路660弄", 31.2726001616614, 121.4479971517318);
        House house_6 = new House("绿苑(闸北)", 31.26883416704542, 121.44054794127722);
        House house_7 = new House("大宁龙盛雅苑", 31.271248468985213, 121.4473411687975);
        House house_8 = new House("八方花苑", 31.27153209497381, 121.44797222392782);
        House house_9 = new House("延长小区(闸北)", 31.267451797691802, 121.44054794127722);
        House house_10 = new House("延长小区(普陀)", 31.267428787563922, 121.44061345283365);

        houseList.add(house_1);
        houseList.add(house_2);
        houseList.add(house_3);
        houseList.add(house_4);
        houseList.add(house_5);
        houseList.add(house_6);
        houseList.add(house_7);
        houseList.add(house_8);
        houseList.add(house_9);
        houseList.add(house_10);

        CompletableFuture<ViewRenderable>[] views = new CompletableFuture[houseList.size()];

        for (int i = 0; i < houseList.size(); i++) {
            CompletableFuture<ViewRenderable> exampleLayout =
                    ViewRenderable.builder()
                            .setView(this, R.layout.example_layout)
                            .build();
            views[i] = exampleLayout;
        }



        CompletableFuture.allOf(
                views)
                .handle(
                        (notUsed, throwable) -> {
                            // When you build a Renderable, Sceneform loads its resources in the background while
                            // returning a CompletableFuture. Call handle(), thenAccept(), or check isDone()
                            // before calling get().

                            if (throwable != null) {
                                DemoUtils.displayError(this, "Unable to load renderables", throwable);
                                return null;
                            }

                            try {
                                for (CompletableFuture<ViewRenderable> completableFuture : views) {
                                    viewRenderables.add(completableFuture.get());
                                }
//                                    exampleLayoutRenderable = exampleLayout.get();
//                                andyRenderable = andy.get();
//                                testLayoutRenderable = testLayout.get();
                                hasFinishedLoading = true;

                            } catch (InterruptedException | ExecutionException ex) {
                                DemoUtils.displayError(this, "Unable to load renderables", ex);
                            }

                            return null;
                        });

        // Set an update listener on the Scene that will hide the loading message once a Plane is
        // detected.
        arSceneView.getPlaneRenderer().setEnabled(false);
        arSceneView
                .getScene()
                .setOnUpdateListener(
                        frameTime -> {
                            if (!hasFinishedLoading) {
                                return;
                            }

                            if (locationScene == null) {
                                locationScene = new LocationScene(this, this, arSceneView);

                                for (int i = 0; i < houseList.size(); i++) {
                                    House house = houseList.get(i);
                                    if (viewRenderables.get(i) != null) {
                                        ViewRenderable viewRenderable = viewRenderables.get(i);
                                        LocationMarker locationMarker = new LocationMarker(house.getLng(), house.getLat(), getNode(house, viewRenderable));
                                        locationMarker.setRenderEvent(new LocationNodeRender() {
                                            @Override
                                            public void render(LocationNode node) {
                                                View eView = viewRenderable.getView();
                                                TextView distanceTextView = eView.findViewById(R.id.textView2);
                                                distanceTextView.setText(node.getDistance() + "M");
                                            }
                                        });
                                        locationScene.mLocationMarkers.add(locationMarker);
                                    }
                                }
                            }


                            Frame frame = arSceneView.getArFrame();
                            if (frame == null) {
                                return;
                            }

                            if (frame.getCamera().getTrackingState() != TrackingState.TRACKING) {
                                return;
                            }
//                            if (first) {
//                                first = false;
//                                LocationMarker testMarker = new LocationMarker(
//                                        121.44061345283365,
//                                        31.26742878756392,
////                                        TransUtil.gcj02towgs84(121.44061345283365,
////                                                31.26742878756392)[0],
////                                        TransUtil.gcj02towgs84(121.44061345283365,
////                                                31.26742878756392)[1],
//                                        getTest());
//                                locationScene.update(testMarker, frame);
//                            }

                            if (locationScene != null) {
                                locationScene.processFrame(frame);
                            }

                        });
        ARLocationPermissionHelper.requestPermission(this);
    }

    @TargetApi(Build.VERSION_CODES.N)
    private Node getNode(House house, ViewRenderable viewRenderable) {
        Node base = new Node();
        base.setRenderable(viewRenderable);
        Context c = this;
        // Add  listeners etc here
        View eView = viewRenderable.getView();
        TextView tv = eView.findViewById(R.id.textView);
        tv.setText(house.getName());
        eView.setOnTouchListener((v, event) -> {
            Toast.makeText(
                    c, house.getName(), Toast.LENGTH_LONG)
                    .show();
            return false;
        });

        return base;
    }


    /**
     * Make sure we call locationScene.resume();
     */
    @Override
    protected void onResume() {
        super.onResume();

        if (locationScene != null) {
            locationScene.resume();
        }

        if (arSceneView.getSession() == null) {
            // If the session wasn't created yet, don't resume rendering.
            // This can happen if ARCore needs to be updated or permissions are not granted yet.
            try {
                Session session = DemoUtils.createArSession(this, installRequested);
                if (session == null) {
                    installRequested = ARLocationPermissionHelper.hasPermission(this);
                    return;
                } else {
                    arSceneView.setupSession(session);
                }
            } catch (UnavailableException e) {
                DemoUtils.handleSessionException(this, e);
                this.finish();
            }
            catch (Exception e){
                Toast.makeText(LocationActivity.this, "exit!!!!", Toast.LENGTH_LONG).show();
                this.finish();
            }
        }

        try {
            arSceneView.resume();
        } catch (CameraNotAvailableException ex) {
            DemoUtils.displayError(this, "Unable to get camera", ex);
            finish();
            return;
        }

        if (arSceneView.getSession() != null) {
//            showLoadingMessage();
        }
    }

    /**
     * Make sure we call locationScene.pause();
     */
    @Override
    public void onPause() {
        super.onPause();

        if (locationScene != null) {
            locationScene.pause();
        }

        arSceneView.pause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        arSceneView.destroy();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] results) {
        if (!ARLocationPermissionHelper.hasPermission(this)) {
            if (!ARLocationPermissionHelper.shouldShowRequestPermissionRationale(this)) {
                // Permission denied with checking "Do not ask again".
                ARLocationPermissionHelper.launchPermissionSettings(this);
            } else {
                Toast.makeText(
                        this, "Camera permission is needed to run this application", Toast.LENGTH_LONG)
                        .show();
            }
            finish();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            // Standard Android full-screen functionality.
            getWindow()
                    .getDecorView()
                    .setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

}
