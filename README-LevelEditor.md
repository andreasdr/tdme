TDMELevelEditor
===============

    - What is it?
        - TDME based level editor 

    - What is already working
    	- model library
        	- model loading
        		- DAE, Wavefront OBJ, TMM meta data files
	        	- setup name, description
    		- render with generated ground, shadowing, bounding volume
        	- setting up model properties
        		- key, value map for what ever reason
    		- setting up the model pivot
    		- create bounding box trigger
    	- level editor
			- setting up a grid where to place objects on
			- load, save maps, showing level dimension
			- setting up map properties
				- key, value map for what ever reason
			- objects
			 	- set up name, description
			 	- see model name, center
		 	- setting up object transformations for a single or multiple objects
		 		- translation
		 		- scaling
		 		- rotation
		 		- color your objects
		 		- center objects
		 		- remove objects
	 		- setting up object properties
	 			- key, value map for what ever reason
			- set up up to 4 lights (for now)
			- see and select models from model library to place them on map
			- rotate, zoom and pan map, selecting objects
			- copy and paste

    - What is WIP
        - Use TDME-GUI instead of Nifty-GUI
        - Load levels from DAE files

    - Technology
    	- uses Nifty-GUI for GUI
        - uses TDME for rendering
        - platforms
            - Windows
            - Linux
            - Mac Os X
            - Android

    - Links
		- Nifty-GUI, http://void256.github.io/nifty-gui/

    - Credits
        - Dominik Hepp
        - Kolja Gumpert
        - others
