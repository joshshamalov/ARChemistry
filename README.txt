# ARChemistry

ARChemistry is an Android application designed to visualize and solve organic chemistry reactions using Augmented Reality (AR). It scans typed line-angle reaction diagrams, identifies reactants and reagents, simulates the reaction logic, and displays a 3D ball-and-stick model of the product molecule in real time using the device camera. This tool is optimized for educational use by chemistry students and researchers and is built to work entirely offline.

## Project Goals
Transform static organic chemistry diagrams into interactive 3D models
Provide a new way to visualize and understand reaction mechanisms
Ensure the entire application functions offline for classroom or field use

## Target Platform
Android (API Level 24+)
APK distribution only (no Play Store publishing for now)
Not currently available on iOS or the web

## Supported Reactions (MVP)
- Hydrogenation of alkenes
- Halogenation of alkenes
- Dihydroxylation of alkenes

These reactions are executed via rule-based logic applied to molecular graphs. No machine learning models are used.

## Input Methods
Typed line-angle diagrams only (handwriting support planned in future)
Users may select a reaction image by either:
- Capturing an image using the app's built-in camera
- Choosing an existing image from the device's camera roll
- After selecting or capturing an image, the user is prompted to crop and optionally rotate it.
- The cropped image is used for processing while the original full-size image is saved in case the user wants to revert.
- An edit icon on the main screen allows the user to re-crop or revert the image.
- The reagent is selected from a dropdown list provided in the app

## Application Flow
1. Reactant Screen
   - User selects or captures an image of the reaction
   - Crops the image to isolate the reactant
   - Chooses the reagent from a dropdown list
   - Clicks "Process Reaction"
   - Reaction is executed in the background
   - A new screen is launched with AR view of the product molecule

2. AR Product Screen
   - Opens camera in real time
   - Displays the product molecule as a 3D ball-and-stick model in space
   - Anchored to surface detected by ARCore (future support for anchoring to image targets)

3. Backend Visualization Screen
   - Clicking the "Backend" button opens a screen that walks the user through each step of the processing pipeline.
   - For every step (e.g. image preprocessing, OCR, masking, bond detection), a short description and an illustrative image are shown.
   - Helps users debug and understand how the system interpreted the uploaded molecule.
   - Opens camera in real time
   - Displays the product molecule as a 3D ball-and-stick model in space
   - Anchored to surface detected by ARCore (future support for anchoring to image targets)

## Pipeline Overview
## OCR and Line-Angle Recognition Logic

ARChemistry uses a dual-pipeline approach to accurately interpret chemical diagrams using two distinct subsystems:

1. ML Kit OCR (Text Recognition)
   - Runs first on the unmodified input image.
   - Detects and extracts all textual chemical symbols and element labels.
   - Each recognized symbol is associated with a bounding box and stored along with its coordinates.
   - Multi-character elements (e.g., Cl, Zn) are joined based on spatial proximity and capitalization rules.
   - These bounding boxes are masked (drawn over with white) before the image is passed to OpenCV.

2. OpenCV (Line-Angle Skeleton Detection)
   - Operates on the image with OCR regions masked out.
   - Uses HoughLinesP to detect all line segments representing chemical bonds.
   - Identifies vertices (bond endpoints and junctions) as candidate atom positions.
   - Each unlabeled vertex is assumed to be a Carbon atom by default.
   - A proximity threshold is used to match OCR-labeled atoms to the nearest node in the structure.

3. Bond Order Detection
   - Uses line clustering to determine bond order:
     - Single line = single bond
     - Two close, parallel lines = double bond
     - Three lines = triple bond
   - Bond types are inferred and stored as edge weights in the multigraph structure.
   - Further refinement of bond types can be optionally verified during CDK-based coordinate generation.

4. Ring System Detection
   - The multigraph is checked for cycles using depth-first search.
   - Any cycles found are marked as ring systems and retained through the 2D and 3D structure generation pipeline.
   - These rings are respected in coordinate generation to maintain realistic geometric constraints.

1. Image Preprocessing
   - Grayscale conversion
   - Binarization and noise reduction
   - Line detection using OpenCV

2. OCR and Structure Recognition
   - Google ML Kit Text Recognition (on-device)
   - Distinguishes chemical symbols from lines
   - Detects atoms (e.g., Na, Cl, Zn)

3. Reactant Graph Construction
   - Undirected multigraph via JGraphT
   - Atoms = nodes; Bonds = edges
   - Initial reactant graph is preserved; a copy is mutated to form the product

4. Reaction Execution
   - Reaction-specific transformations applied to graph
   - Results stored as a second undirected multigraph (the product)

5. Coordinate Generation
   - CDK (Chemistry Development Kit v2.11) is used to compute 3D coordinates
   - Selective integration: Only non-AWT modules are used to avoid Android compatibility issues
   - Atom and bond positions are extracted from CDK output

6. 2D Visualization
   - Reactant and product can be viewed in 2D using Jetpack Compose Canvas
   - Used as a debugging and educational tool

7. AR Rendering
   - SceneView and ARCore used to render 3D product molecule
   - Displayed in real-time camera space using ARCore plane tracking


## Development Environment (Verified)

- **Android Studio**: Meerkat 2024.3.1 Patch 1
- **Gradle**: 8.11.1
- **Android Gradle Plugin (AGP)**: 8.9.1
- **Gradle JDK**: Eclipse Temurin 17.0.4
- **Kotlin**: 2.1.20


## Tech Stack

Component | Technology | Version
----------|------------|--------
Language | Kotlin | 2.1.20
UI | Jetpack Compose | 1.7.8
Graph Structure | JGraphT | 1.5.2
Chemistry Engine | CDK (non-AWT modules) | 2.11
OCR | ML Kit Text Recognition | v2
Image Processing | OpenCV Android SDK | 4.9.0
AR Rendering | SceneView | 2.2.1
AR Engine | ARCore | 1.48.0

## Constraints and MVP Scope
- Molecule limit: max 10 carbon atoms (plus additional atoms like H, Cl, Br, etc.)
- No stereochemistry, wedges, or dashes in MVP
- 3D layout is implied through atom positioning, not explicit 3D bond notation
- Only the product molecule is shown in AR
- No export, save, or user feedback options yet
- Errors should be displayed to the user when components fail (e.g., OCR failure, structure parsing issues)

## Development Setup

### Prerequisites
Android Studio Meerkat (2024.3.1 Patch 1)
OpenJDK 21.0.5
Gradle (bundled with Android Studio)
Android device or emulator with ARCore support

### Build Instructions
Open in Android Studio
Sync Gradle and build the project
Run on a physical ARCore-compatible device for full AR functionality

## Code Organization
The project should follow a modular folder structure:
- ocr/ - OCR and text recognition components
- graph/ - Data structures and graph manipulation logic
- reaction/ - Reaction rule engine and graph transformation
- coordinates/ - CDK 3D coordinate generation
- rendering/ - AR rendering and 2D visualization logic
- ui/ - User interface components (Jetpack Compose)
- utils/ - Shared helper functions and constants
- tests/ - Unit tests for core logic

## Testing
To ensure all major components work as intended, unit tests should be written for each step of the pipeline:
- Line-angle recognition and OCR parsing (combined test for detecting atom labels and bond structures)
- Reactant multigraph creation using JGraphT
- Chemical transformation using selected reagent rules
- 2D graph rendering and structure visualization
- 3D coordinate generation using CDK and preparation of data for AR rendering
- Successful rendering of product molecule in AR using SceneView and ARCore

These tests should verify that functionality behaves correctly under normal usage. Testing for invalid inputs or failures is optional and may be considered in future phases.

## Future Work
- Handwriting recognition (custom OCR training or ML integration)
- AR visualization of entire reaction (reactants, reagents, and product)
- Gesture-based molecule rotation and manipulation
- Exporting molecular data and models
- Support for wedge/dash notation and stereochemistry
- Reaction animation and step-by-step mechanism views
- Analytics and usage tracking

## Author
This is a solo thesis project built with the guidance of academic advisors. Development is being conducted using Kotlin and VSCode with Augment AI as a co-developer.

## Augment AI Instructions
This README contains all technical and architectural information needed to begin development. Please start by:
1. Initializing the project using the specified tech stack
2. Setting up module separation as listed in the Code Organization section
3. Building out MVP-level support for three reactions listed above
4. Creating the image capture/cropping UI with dropdown reagent selection and "Process Reaction" button
5. Triggering a new screen to open the camera and render the product in AR using SceneView
6. Writing unit tests as described and implementing error handling for all major pipeline failures

All core functionality must work offline. Reach out via repo issues if clarification is needed.
## Backend Visualization System (Step-by-Step)

Each step in the image processing and molecule-building pipeline is documented with both a visual snapshot and a short description. This helps with debugging, transparency, and education.

Step 1: Image Input & Reagent Selection
Description:
The user selects or captures a reaction image. They are prompted to crop and optionally rotate the image to focus only on the reactant. A reagent is selected from the dropdown.
Visual: Cropped input image.

Step 2: Image Preprocessing
Description:
The selected image is processed by OpenCV: converted to grayscale, denoised, and enhanced to prepare for text and structure recognition.
Visual: Grayscale, binarized image after preprocessing.

Step 3: Text Recognition / Bounding Boxes
Description:
ML Kit scans the image for chemical symbols. For each symbol detected, a blue bounding box is drawn.
Bounding box coordinates and text labels are saved for the next masking step.
Visual: Preprocessed image overlaid with blue bounding boxes.

Step 4: Atomic Symbol Text Masking
Description:
Text within the blue bounding boxes is masked out using OpenCV, but the boxes remain as placeholders for those atom locations.
Visual: Image with labeled text removed, but blue boxes still visible.

Step 5: Line-Angle Structure Recognition
Description:
OpenCV detects bond lines in the masked image. Green lines represent detected bonds. Red circles are drawn at junctions and endpoints (representing implicit Carbon atoms).
Coordinates of all vertices and lines are recorded.
Visual: Green lines overlaying the structure, with red dots at each vertex.

Step 6: Reactant Graph Construction
Description:
An undirected multigraph is created using JGraphT. Nodes represent atoms (explicit or implicit), and edges represent bonds.
This graph is preserved as the original reactant graph.
Visual: 2D graph render showing atom labels and bond connections.

Step 7: Reaction Execution
Description:
A copy of the multigraph is passed into the reaction engine. The selected reagent’s logic is applied, and the structure is modified accordingly.
Visual: Updated 2D graph representing the product molecule.

Step 8: Coordinate Generation
Description:
CDK is used to convert the 2D graph into a 3D coordinate model. Only non-AWT modules are used for Android compatibility.
Visual: Optional 3D coordinate output as raw data or preview (if feasible).

Step 9: AR Rendering
Description:
SceneView and ARCore render the final molecule in real-time.
The 3D model is positioned on a detected surface using plane tracking.
Visual: Live AR display of the product molecule in the camera view.


## Debug Logging System

A persistent debug log system is integrated into the backend pipeline. Each step of the processing pipeline appends messages to a structured debug log.

- Errors, warnings, and status updates are recorded in real time.
- Users can access the full log by clicking the "View Debug Logs" button from the main screen.
- The Debug Log screen provides a scrollable view of all logged messages from the current session.
- A button allows users to copy all debug log content to the clipboard for sharing or analysis.
