This folder contains all templates used by ANT to build this project (see $root/build.xml).

Those templates will be copies by the ANT-build command to the source directories in following order:
  1. Copy files which are specific for the ANT-build command. Files in folder "debug" will only be 
     copied in case command "ant <folder>" is used. Files in folder "release" will only be copied in  
     case command "ant release" is used. Its primary usage now is to specify different resource id for
     the leaderboards depent on the mode in which the app was build.
  2. Files in folder "all" will be copied regardless of which ant build command is used. This folder 
     contains source code which will be substituted with variables stored in file "ant.properties". Files
     in the all folder will overwrite files copies in the previous step.  
