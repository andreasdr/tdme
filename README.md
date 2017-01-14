TDME
====

    - What is it?
        - ThreeDeeMiniEngine is a small 3d engine suited for small 3d games written in JAVA

    - What is already working
        - 3d engine
            - model parsing
                - WaveFront OBJ
                - DAE parsing with skinned meshes and animations
                    - group names/ids must not have whitespace characters
                    - animations
                        - requires baked matrices
                - requires triangulated meshes for now
            - object transformations
                - scaling
                - rotations
                - translation
            - animations
                - skinning can be computed on CPU or GPU
                - supports base animation and animation overlays
                - supports attaching objects to bones of another objects
            - color effects on objects
                - via shader
                - color addition
                - color multiplication
            - lighting via shaders
            	- supports phong lighting
            	- supports phong shading on GL3, GL2
            	- supports gouraud shading on GLES2
            	- supports diffuse mapping on GL3, GL2, GLES2
            	- supports specular shininess mapping on GL3
            	- supports normal mapping on GL3
            - dynamic shadows via shaders
            - particle system which
              - is object based
              - or point based
              - and supports
                - basic/point emitter
                - sphere emitter
                - bounding box emitter
                - circle on plane emitter
            - camera control
              - set up look from, look at, up vector can be computed
              - frustum culling
                - oct tree like partitioning from 16mx16mx16m up to 4mx4mx4m
            - object picking
            - supports offscreen instances
                - rendering can be captured as screenshot
                - rendering can be used (in other engine instances) as diffuse texture
            - screenshot ability
            - multiple renderer
              - GL2, GL3(core) and GLES2
        - 3d audio
            - decoder
              - ogg vorbis decoder
            - audio entities
              - streams
              - sounds
        - physics
            - discrete collision detection
                - sphere
                - capsule
                - axis aligned bounding boxes
                - oriented bounding boxes
                - triangle
                - convex mesh
            - rigid body simulator
              - broadphase collision detection
              	- uses oct tree like partitioning from 16mx16mx16m up to 4mx4mx4m
              	- additionally sphere <> sphere test
              - narrowphase collision detection
              - collision filtering by type
              - sleeping technology

    - What does it (maybe still) lack
        - GUI system
    	- own more efficient model file format
        - animation blending
        - physics
          - multiple bounding volumes for a rigid body
          - rag doll / joints / springs
        - example games
        - documentation

    - What is WIP
        - GUI system
        - own more efficient model file format
        - rigid body simulator(needs to be updated to newer "ReactPhysics3D 0.5")

    - Technology
        - designed for simple multi threading
        	- 3d engine uses one thread for now
        	- physics or game mechanics can run in a separate thread(s)
        - uses JOGL, JOAL, JOrbis-0.0.17
        - platforms
            - Windows
            - Linux
            - Mac Os X
            - Android

    - Tools
		- TDME Model Viewer, see [README-Viewer.md](https://github.com/andreasdr/tdme/blob/master/README-Viewer.md)
		- TDME Level Editor, see [README-LevelEditor.md](https://github.com/andreasdr/tdme/blob/master/README-LevelEditor.md)

    - Links
        - <youtube link here>
		- TDME Android, https://github.com/andreasdr/tdme-android

    - References
        - "game physics - a practical introduction" / Kenwright
        - "real-time collision detection" / Ericson
        - "ReactPhysics3D" physics library, http://www.reactphysics3d.com 
        - the world wide web! thank you for sharing

    - Other credits
        - Dominik Hepp
        - Mathias Wenzel
        - Sergiu Crăiţoiu
        - others
       