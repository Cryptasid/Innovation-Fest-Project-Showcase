FIRE STATION MANAGEMENT SYSTEM — PHASE 2
=======================================

Requirements
------------
- Windows 10/11
- JDK 21 (Temurin 21 recommended)
- JavaFX SDK 21 for Windows x64
- MySQL 8.x / XAMPP
- VS Code with the Extension Pack for Java

Libraries already included in /lib:
- mysql-connector-j-26.7.0.jar
- jcalendar-1.4.jar

JavaFX modules used:
- javafx.controls
- javafx.web
- javafx.swing

The local map HTTP server uses JDK module jdk.httpserver, and the JavaScript bridge uses jdk.jsobject; neither adds a third-party dependency.

DATABASE
--------
Database: db_firestation_professional
Default connection (DBConnection.java):
- Host: localhost:3306
- User: root
- Password: empty

For a new installation:
1. Start MySQL.
2. Run database.sql in phpMyAdmin or MySQL CLI.
3. database.sql uses CREATE TABLE IF NOT EXISTS and contains no DROP statements.

For an existing Phase 1 installation:
1. Do NOT rerun database.sql as an upgrade script.
2. Select db_firestation_professional in phpMyAdmin.
3. Run upgrade_incident_coordinates.sql once. It adds nullable latitude and longitude columns without dropping or recreating any records or tables.
4. Your Phase 1 Vehicle module (tbl_vehicle, VehicleDAO, VehicleForm, optional vehicle_id in tbl_dispatch) is retained. Do not rerun upgrade_vehicle_feature.sql if that vehicle upgrade has already been applied.

The migration does not modify tbl_firefighter, tbl_vehicle, tbl_dispatch, or their foreign-key relationships.

GOOGLE MAPS API KEY (LOCAL CONFIGURATION)
-----------------------------------------
The key is deliberately not included in the project ZIP.
1. Copy config/google-maps-config.example.js.
2. Rename the copy to config/google-maps-config.js.
3. Open config/google-maps-config.js and replace YOUR_GOOGLE_MAPS_API_KEY with your own key:

   window.FSMS_GOOGLE_MAPS_API_KEY = 'YOUR_GOOGLE_MAPS_API_KEY';

4. Save the file. The local MapServer serves this configuration to the embedded WebView.

config/google-maps-config.js is listed in .gitignore. Keep it ignored and never commit it. The example file is safe to commit because it contains only a placeholder.

A browser-based Maps JavaScript API key is visible to the WebView/browser at runtime by design. Keep its website/referrer restriction enabled and restrict it to the Maps JavaScript API and Places API (New). This project serves the map from:

   http://127.0.0.1:8765/map.html

Your existing authorized referrer pattern http://127.0.0.1/* is used. Google documents that omitting a port from a website restriction pattern allows requests from any port for that host. If you prefer localhost, update MapServer.java and the authorized referrer consistently. Google Maps requires valid billing and API authorization for live tiles and Places requests; enabling APIs alone does not guarantee that requests will succeed.

The search box uses the new PlaceAutocompleteElement widget and Place.fetchFields() flow from the Places library (Places API (New)); it does not use the deprecated legacy Places Autocomplete class. Custom incident pins use AdvancedMarkerElement with the Google demo map ID. If you create a project Map ID for production styling, replace DEMO_MAP_ID in map.html with your own Map ID.

RUN IN VS CODE (WINDOWS)
------------------------
1. Extract the ZIP.
2. Open the extracted FireStationManagement_Complete folder (the folder containing MainForm.java) in VS Code.
3. Confirm JDK 21 is selected by the Java extension.
4. Confirm JavaFX SDK 21 is installed at:

   %USERPROFILE%\Downloads\openjfx-21_windows-x64_bin-sdk\javafx-sdk-21\lib

   The supplied .vscode settings, launch configuration and task use this USERPROFILE-based path. If your JavaFX SDK is in another folder, update the path in .vscode/settings.json, .vscode/tasks.json and .vscode/launch.json.

5. Configure MySQL in DBConnection.java if your local username/password differs.
6. Run the required database migration described above.
7. Copy and edit config/google-maps-config.js with your key.
8. In VS Code, use Run and Debug and select:
   Fire Station Management System (Java 21 + JavaFX 21)

The launch task compiles MainForm.java and its Java source dependencies into bin, then starts MainForm with JavaFX modules on the module path. The MySQL and JCalendar JARs remain on the classpath.

You can also use the VS Code build task (Terminal > Run Build Task) to compile without launching.

MAP FEATURES
------------
- Incident rows can store nullable latitude and longitude values.
- IncidentForm has Select Location on Map and Clear Map Location actions.
- The picker supports Places search (New) and map clicks; the marker's coordinates are returned only after Confirm Location is clicked.
- Map View is embedded in the MainForm content area via JavaFX JFXPanel and WebView.
- Map markers are generated only from geolocated MySQL incidents. Markers are red with a white exclamation mark and the map fits the available incidents.
- Marker information includes incident ID, threat, severity, street, city, status and incident date.
- Active / investigating incidents can open the existing DispatchForm with that incident preselected. Resolved incidents are shown but dispatch is disabled, matching the existing Dispatch form's non-resolved incident choices.
- Existing firefighter, incident, dispatch and vehicle forms/queries remain in place. The vehicle assignment workflow and validation remain the Phase 1 implementation.

TESTING NOTES / LIMITATIONS
---------------------------
The project must be run on Windows with the supplied JavaFX SDK path and a working MySQL instance to validate the full desktop flow. Live Google Maps tiles and Places search also require network access, a valid restricted key, enabled APIs and active billing. No claim of live Google Maps authentication or database CRUD testing is made by this source package alone.
