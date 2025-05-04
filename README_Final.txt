# ARChemistry (Revised - Imago OCR / RDKit Server)

## Project Overview

ARChemistry is an Android application designed to visualize organic chemistry reactions using Augmented Reality (AR). It allows users to capture or select images of **typed** line-angle reaction diagrams. The application sends the image and a selected reagent name to a backend server. The server uses **Imago OCR** to interpret the chemical structure from the image (likely as a Molfile) and then utilizes the **RDKit** cheminformatics toolkit to perform the specified chemical reaction, generate 3D coordinates for the product molecule, and extract the final structure data (atoms, coordinates, bonds). This 3D data is returned to the Android app, which then displays an interactive 3D ball-and-stick model of the product molecule in AR using ARCore and SceneView. The application is intended for educational use. **Note:** Structure recognition, reaction simulation, and 3D coordinate generation now require an **internet connection** to communicate with the backend server. AR display occurs on the device using the received data.

## Project Goals

*   Transform static 2D organic chemistry diagrams into interactive 3D AR models.
*   Provide an intuitive way to visualize and understand reaction outcomes.
*   Utilize server-side **Imago OCR** for structure recognition from images.
*   Utilize server-side **RDKit** for reaction simulation and 3D coordinate generation.
*   Render the final 3D product molecule in AR on the Android device.

## Target Platform

*   Android (API Level 24+).
*   Distribution via APK only (no Google Play Store publishing planned).
*   No current plans for iOS or web versions.

## Supported Reactions (Server-Side via RDKit)

*   Hydrogenation of alkenes
*   Halogenation of alkenes (specifically Br2 addition as implemented)
*   Dihydroxylation of alkenes (specifically syn-dihydroxylation as implemented)
*   Reaction logic defined using SMARTS patterns within the RDKit library on the server.

## Input Methods

*   **Input Type:** Typed line-angle diagrams only.
*   **Image Source:** Camera capture or gallery selection.
*   **Image Cropping:** Required using `Android Image Cropper v4.6.0`.
*   **Reagent Selection:** Predefined dropdown list on the Android app.

## Core Application Flow (Current Implementation)

1.  **Reactant Screen (App):** User captures/selects/crops image, selects reagent name from dropdown, initiates processing.
2.  **Network Communication (App -> Server):**
    *   App shows progress indicator.
    *   App sends cropped image file and selected reagent name string via **Retrofit** as a multipart POST request.
3.  **Server Processing (`archem_server/app.py`):**
    *   **Flask server** receives image and reagent name.
    *   Server uses **Imago OCR** (`imago_console.exe`) to convert image to structure (Molfile).
    *   Server uses **RDKit** to read Molfile, apply the specified reaction based on `reagent_name`, add hydrogens, generate 3D coordinates, and optimize the structure.
    *   Server extracts atom symbols, 3D coordinates, and bond information.
    *   Server returns this 3D structure data (`atoms`, `coords`, `bonds`) or an error message in a JSON response.
4.  **Network Communication (Server -> App):**
    *   App receives JSON response via **Retrofit**.
5.  **Data Handling & Navigation (App):**
    *   App parses the JSON response using **Gson**.
    *   App checks for errors. On success, it serializes the received 3D data back into a JSON string.
    *   App navigates to the AR screen, passing the JSON string containing the 3D data.
6.  **AR Product Screen (App):**
    *   Receives and parses the 3D data JSON string.
    *   Uses the `atoms`, `coords`, and `bonds` data to render the 3D product model using **ARCore** and **SceneView**.
7.  **Backend Visualization Screen (App):** Shows pipeline stages and intermediate data (e.g., server response status).

## Processing Pipeline Overview (High-Level - Current Implementation)

*(Detailed step-by-step guide in `ARChemistry_Pipeline_Final.txt`)*

1.  **Image Input (App):** Capture/select, crop (`Android Image Cropper v4.6.0`).
2.  **Reagent Selection (App):** Select reagent name from dropdown.
3.  **Network Send (App):** Send image and reagent name to server (Retrofit).
4.  **Server Processing (Server):** Image-to-Structure (Flask + **Imago OCR**), Structure Reading, Reaction Simulation, H Addition, 3D Coordinate Generation, Optimization (**RDKit**). Data Extraction (atoms, coords, bonds).
5.  **Network Receive (App):** Receive JSON response with 3D data or error (Retrofit, Gson).
6.  **AR Rendering (App):** Parse 3D data, render model (ARCore `1.48.0`, SceneView `2.3.0`).

## Development Environment (Target)

*   **Android Studio:** Iguana | 2023.2.1 Patch 1 or newer compatible version.
*   **Gradle:** **`8.8`** (via wrapper). *(Current version)*
*   **Android Gradle Plugin (AGP):** **`8.6.0`**. *(Current version)*
*   **Gradle JDK:** Java `17`.
*   **Kotlin:** `1.9.22`+.
*   **Server:** Python 3.x environment managed by **Conda (Anaconda Distribution or Miniconda recommended)**. Requires Flask, **Imago OCR** (plus its dependencies, installed separately), and **RDKit**.

### Server Environment Setup Recommendation (Using Conda)

It is highly recommended to use Conda (preferably Miniconda for a minimal installation, although Anaconda Distribution also works) to manage the Python environment and dependencies on the server. This simplifies the installation of complex packages like RDKit.

1.  Install Anaconda Distribution or Miniconda on the server (Windows, macOS, or Linux).
2.  Create a dedicated Conda environment for this project (replace `archem-server-env` and `3.10` as needed):
    `conda create --name archem-server-env python=3.10`
3.  Activate the environment:
    `conda activate archem-server-env`
4.  Install required packages into the active environment. Using an `environment.yml` file is recommended for reproducibility:
    ```yaml
    name: archem-server-env # Name used if creating from file, ignored if updating
    channels:
      - conda-forge # RDKit is typically found here
      - defaults
    dependencies:
      - python=3.10 # Or your target Python version
      - flask # Python web framework
      - rdkit # Cheminformatics toolkit
      - requests # For test_client.py
      - pip # Include pip for installing packages not on conda channels if needed
      # Add other Python dependencies like gunicorn (for production deployment)
      # Note: Imago OCR must be installed separately following its own instructions.
      # Ensure its dependencies (e.g., specific C++ redistributables on Windows) are also met.
      # - pip:
      #   - <any pip-only packages>
    ```
    Install using: `conda env create -f environment.yml` (if creating new) or install individually into an active environment:
    `conda install flask requests`
    `conda install -c conda-forge rdkit`
5.  Install Imago OCR (the command-line utility) separately following its specific instructions. Place the executable (e.g., `imago_console.exe`) in a known location accessible by the server script (e.g., a `tools` subfolder within the server project). Ensure any runtime dependencies for Imago OCR are met on the server system.

### Local Development Server Setup

*(Ensure correct tool names and Conda activation)*

For development, run the **Imago OCR / RDKit / Flask** server locally.

1.  **Network:** Ensure laptop and Android device/emulator are on the same Wi-Fi.
2.  **Server IP:** Find laptop's local network IP (e.g., `192.168.x.x`).
3.  **Activate Conda Env:** Open Anaconda Prompt (or terminal), run `conda activate archem-server-env`.
4.  **Run Flask Server:** Navigate to the server project directory (`archem_server`) and run the Flask app: `python app.py`. The script should configure Flask to listen on `0.0.0.0:5000`.
5.  **Retrofit Configuration (App):** Set base URL in `core/network/RetrofitClient.kt` to `http://<YOUR_LAPTOP_IP>:5000/`.
6.  **Firewall:** Allow incoming connections on port 5000 on the server machine.
7.  **Android Network Security:** Ensure `network_security_config.xml` allows cleartext traffic to your local IP/emulator alias (`10.0.2.2`) if using HTTP.

## Tech Stack (Current Libraries & Versions)

| Component                  | Technology                     | Final Version            | Notes / Repository / Artifact                                                                                              |
| :------------------------- | :----------------------------- | :----------------------- | :------------------------------------------------------------------------------------------------------------------------- |
| Language (App)             | Kotlin                         | **`1.9.22`+** |                                                                                                                            |
| UI Framework               | Jetpack Compose Core           | **`1.6.7`+** | (Use BOM `androidx.compose:compose-bom:2024.05.00` or later)                                                               |
| UI Framework               | Jetpack Compose Material 3     | **`1.2.1`+** |                                                                                                                            |
| Navigation                 | Navigation Compose             | **`2.7.7`+** | `androidx.navigation:navigation-compose`                                                                                   |
| Image Cropping             | Android Image Cropper          | `4.6.0`                  | `com.vanniktech:android-image-cropper`                                                                                     |
| **Networking (App)** | **Retrofit** | **`2.11.0`** | `com.squareup.retrofit2:retrofit` |
| **Networking (App)** | **OkHttp Logging Interceptor** | **(Match OkHttp)** | `com.squareup.okhttp3:logging-interceptor` (For debug builds only)                                                         |
| **Networking (App)** | **JSON Converter (Retrofit)** | **Gson** | `com.squareup.retrofit2:converter-gson` (Compatible with Retrofit 2.11.0)                               |
| AR Engine                  | ARCore SDK                     | `1.48.0`                 | `com.google.ar:core`                                                                            |
| AR Scene Rendering         | SceneView (Android AR)         | **`2.3.0`** | `io.github.sceneview:arsceneview` Targets ARCore 1.48. |
| **Server Env Management** | **Conda / Anaconda / Miniconda**| **(Latest Stable)** | **Recommended** for managing Python environment and RDKit.                                           |
| **Server Framework** | **Flask (Python)** | **(Latest Stable)** | Python web framework for the backend. Install via Conda.                                                               |
| **Image Recognition (Server)**| **Imago OCR** | **(Latest Stable)** | Optical Structure Recognition command-line tool. Requires separate installation. |
| **Cheminformatics (Server)**| **RDKit** | **(Latest Stable)** | **Required** for structure reading, reaction simulation, 3D generation. Install via Conda (`conda-forge`). |
| ~~Chemistry Toolkit (App)~~| ~~CDK~~                        | ~~`2.9`~~                | *Removed dependency from Android App* |
| ~~Image Processing~~       | ~~OpenCV (Android AAR)~~       | ~~`4.11.0`~~             | *Removed dependency from Android App* |
| ~~OCR~~                    | ~~ML Kit Text Recognition~~    | ~~`16.0.1`~~             | *Removed dependency from Android App* |
| ~~Graph Representation~~   | ~~JGraphT (Core)~~             | ~~`1.5.2`~~              | *Removed dependency* |

*(Set Compose Compiler version compatible with Kotlin version, e.g., `1.5.10` for Kotlin `1.9.22`)*
*(Ensure all listed dependencies are correctly added to the app's `build.gradle.kts` or `build.gradle` file)*
*(Ensure server environment includes Python, Conda (recommended), Flask, Imago OCR, RDKit)*

## Constraints and MVP Scope (Current)

*   **Molecule Complexity:** Limited by **Imago OCR's** interpretation and **RDKit's** processing capabilities.
*   **Stereochemistry:** Not explicitly handled in reaction logic or AR display in MVP.
*   **AR Display:** Only final *product* molecule displayed in MVP.
*   **User Features:** No export, save state, etc., in MVP.
*   **Error Handling:** Implement robust handling for network, server, **Imago OCR**, and **RDKit** failures.
*   **Server Dependency:** Requires network access to the backend server. Handle unavailability gracefully.

## Code Organization (Current Modular Structure)

*   `app/` - Main application module
*   `core/` - Shared utilities, network setup (Retrofit, ApiService, DTOs), Logging
*   `feature_reactant/` - Reactant screen UI/ViewModel
*   `feature_ar_product/` - AR screen UI/ViewModel
*   `feature_backend_vis/` - Backend visualization screen UI/ViewModel
*   `lib_chemistry/` - *(Likely unused or contains outdated CDK logic)*
*   `lib_testing/` - Shared testing utilities
*   **(Separate Repository/Project):** `archem_server/` - Contains Flask server code (`app.py`), Imago OCR executable (e.g., in `tools/`), RDKit logic, `environment.yml` (for Conda).

## Testing Strategy

*   **Unit Tests (App):** Cover ViewModels (mocking ApiService), UI state logic.
*   **Server Tests:** Unit/integration tests for Flask routes, **Imago OCR** interaction (mocking subprocess), and **RDKit** processing logic.
*   **Integration Tests (App):** Verify UI -> ViewModel -> Network -> Mock Server Response -> Navigation flow.
*   **End-to-End Testing:** Requires physical device and local server running (in activated Conda environment). Test full flow: image capture/select -> crop -> reagent select -> process -> AR display.

## Augment AI Instructions & Priorities (Reflecting Current State)

1.  **Project Setup:** Initialize Android project (Kotlin `1.9.22`+, Compose, AGP `8.6.0`, Gradle `8.8`). Setup modules. Add Android dependencies (Compose, Navigation, Retrofit, Gson, ARCore, SceneView, Cropper). Configure Network Security.
2.  **Server Setup (Conda Recommended):** Set up separate Flask server project (`archem_server`). **Install Anaconda or Miniconda.** Create & activate Conda environment (e.g., `archem-server-env` with Python 3.10 using `environment.yml` or `conda create`). Install Flask & **RDKit** (`conda-forge` channel). **Install Imago OCR command-line utility** following its instructions; place executable (e.g., in `archem_server/tools/`) ensuring any system dependencies are met. Implement Flask endpoint `/process_image` in `app.py`: receive image & reagent name -> save temp image -> call **Imago OCR** (`subprocess`) -> read output Molfile -> use **RDKit** for reaction, H addition, 3D generation, optimization -> extract atoms/coords/bonds -> return JSON. Test Imago OCR and RDKit processing independently first.
3.  **Core Flow & UI (App):** Build Compose UI screens (`ReactantScreen`, `ArProductScreen`) and Navigation graph. Implement image capture/selection, cropping, reagent dropdown.
4.  **App-Server Communication (PRIORITY):** Implement Retrofit service (`ApiService`), DTO (`ModelDataResponse`), client (`RetrofitClient`). Implement ViewModel logic (`ReactantViewModel`) to send image/reagent name and receive/handle 3D data JSON response. Handle loading/errors. Implement navigation logic.
5.  **AR Rendering (App):** Integrate ARCore (`1.48.0`) / **SceneView (`2.3.0`)** in `ArProductScreen`. Parse received 3D data JSON. Dynamically create `SphereNode`s and `CylinderNode`s based on atoms, coords, bonds. Handle AR permissions/transforms. Configure lighting (e.g., low-intensity directional light).
6.  **Backend Visualization Screen:** Implement concurrently if desired.
7.  **Error Handling & Logging:** Implement robust error handling (network, server codes, **Imago OCR**, **RDKit**, AR). Implement Debug Log screen.
8.  **Testing:** Write unit and integration tests.
**Key Constraints Reminder:** Core chemical processing relies on the network/server. Handle errors gracefully.

## Debug Logging System

*   Log significant steps using Android `Log`.
*   Dedicated "Debug Log" screen (scrollable, copyable).

## Future Work Considerations

*   Handwriting recognition, complex diagrams, stereochemistry handling (server & client), reactant display, gestures, animation, caching, data export.