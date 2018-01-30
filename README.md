# SoundMap
An application created by The Ohio State University Department of Speech and Hearing Sciences. This app uses the microphone on an Android microphone to measure sound pressure levels (SPL), expressed as a dB(A) value. This enables researchers and city planners to identify areas in cities that have unusually high levels of ambient noise, and can help reduce the amount of city noise through increased awareness and planning. Before uploading data to the database, users must calibrate their microphones at a lab in Pressey Hall on OSU's main (Columbus) campus. Users can still record and take an approximate measurement without going through this calibration process.

# Specifications
Requires Android Lollipop (5.0) and above. Optimized for the latest version of Android, 8.1. Uses the Android AudioRecord API to record the dB(A) measurement. 

# Permissions
Record audio: to take measurements with the microphone
Location: to record the location of the measurement
Storage: for debug purposes. Will remove soon.

# Dependencies
Special thanks to the JTransforms team for providing a great implementation of the DFT algorithm. 
