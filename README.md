MISExercise3 : OpenCV face detection and Clown Nose effect

Project Setup
I followed this tutorial while setting uo the project
https://medium.com/easyread/integrating-your-android-app-with-opencv-library-266b2ea913d3

But faced some issues while building the project as some dependencies were not getting resolved in gradle file.
Came across this post which solved my problems.
https://stackoverflow.com/questions/55601598/including-opencv-in-android-studio-project-yields-unresolved-dependencies-erro

So the app is successfully running without installing OpenCV Manager app.



CLown Nose Effect
(Correct size of the red circle)
The HaarCascade detect the faces and returns a matrix of detected faces.Each element of the matrix is a Rectangle with which boundary of the image can be determined.
So the red circle is drawn at the center of the detected rectangle and the size of circle is determined by taking radius as quarter of the height of the rectangle.
