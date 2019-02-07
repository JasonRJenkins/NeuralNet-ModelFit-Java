# NeuralNet-ModelFit-Java
Implements a single hidden layer feedforward neural network and uses it to fit a model to a selectable dataset.

This is a Java version of the NeuralNet-ModelFit project - https://github.com/JasonRJenkins/NeuralNet-ModelFit.

Unlike the C++ version this version implements a form based GUI that allows the user to explore the various settings that can be applied to a simple single hidden layer neural network that can be used to model the potential relationship between a single predictor variable (X) and a single corresponding response variable (Y) chosen from a selected .CSV data file.

The project has been built with both the NetBeans IDE 8.2 and the Eclipse IDE for Java Developers (version 2018-12).

To build using NetBeans: 1) Download the source and copy the ModelFit folder and all its contents to your desired location. 2) Start the NetBeans IDE and select "Open Project..." from the File menu. 3) Use the Open Project dialog to locate the ModelFit folder and press the Open Project button. 4) The ModelFit Project should now be available for selection in the IDE - allowing you to go ahead and build it.

To build using Eclipse: 1) Download the source and copy the ModelFit folder and all its contents to your desired location. 2) Start the Eclipse IDE and select "Open Projects from File System..." from the File menu. 3) Import the source by pressing the Directory... button and locating the ModelFit folder using the Browse for Folder dialog box. 4) Click the Finish button and the ModelFit Project should now be available for selection in the IDE - allowing you to go ahead and build it.

The same three datasets used by the C++ version have been included: Auto.csv; Credit.csv and Wage.csv. But you are not restricted to these datasets - you can load any dataset you like if it is available in .CSV format. The desired dataset is loaded into the application via an 'Open' file chooser accessed through the 'Browse' button and once the various model settings have been selected a model can be fitted to the data using the 'Fit Model' button.  Once training is complete and a model has been fitted to the data you can save the model output to a .CSV file. The output consists of 3 columns - the first contains the selected training set input (or predictor) values, the second the selected training set target values and the third contains the trained model output responses to the given input values. To plot the fitted model the output file can be loaded into Excel and a scatter graph can be plotted to see how well the model fits the data. The trained network can also be serialised and saved to a file.

1) Using the Auto.csv dataset the relationship between 'horsepower' and 'mpg' can be modelled using the following settings:

Predictor Variable (X) = horsepower, Response Variable (Y) = mpg

Output Layer: Activation Function = Elliot, Slope = 35, Amplify = 1

Hidden Layer: Activation Function = ISRU, Slope = 5, Amplify = 40

The Main Settings should be left with their default values. In this example the application will converge to a solution very quickly.

2) Using the Credit.csv dataset the relationship between 'Balance' and 'Rating' can be modelled using the following settings:

Predictor Variable (X) = Balance, Response Variable (Y) = Rating

Output Layer: Activation Function = Elliot, Slope = 10, Amplify = 1

Hidden Layer: Activation Function = SoftPlus, Slope = 10, Amplify = 10

In the Main Settings set the Min. Network Error to 700, the Scale Factor to 2000 and leave all the other settings with their default values. This example will also converge to a solution fairly quickly

3) Using the Wage.csv dataset the relationship between 'age' and 'wage' can be modelled using the following settings:

Predictor Variable (X) = age, Response Variable (Y) = wage

Output Layer: Activation Function = Elliot, Slope = 10, Amplify = 1

Hidden Layer: Activation Function = Unipolar, Slope = 10, Amplify = 10

In the Main Settings set the Min. Network Error to 2450, the Scale Factor to 1000, the Number of Iterations to 10000, the Initial Range to 10 and leave all the other settings with their default values. In comparison to the other examples this example will converge to a solution fairly slowly owing to the large size of the dataset.

