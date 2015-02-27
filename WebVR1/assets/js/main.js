// Be the main javascript file here
window.onload= function(){

    // Video stream information
    var video = document.createElement('video');
    video.width = 320;
    video.height = 240;
    video.autoplay = true;

    var camera, scene, renderer;
    var effect, controls;
    var element, container;

    var videoTexture;

    var clock = new THREE.Clock();

    init();
    animate();

    function init(){

        // init for the webcam

        navigator.getUserMedia_ = (   navigator.getUserMedia
                                || navigator.webkitGetUserMedia
                                || navigator.mozGetUserMedia
                                || navigator.msGetUserMedia);



        if (navigator.getUserMedia_) {
            navigator.getUserMedia_({audio: false, video: true}, function(stream) {
                video.src = window.URL.createObjectURL(stream);
            }, function(){
                console.log('Error loading the video');
            });
        } else {
            console.log('Error loading the video');
        }


        // init for the THREE
        renderer = new THREE.WebGLRenderer();
        element = renderer.domElement;
        container = document.getElementById('canvers');
        container.appendChild(element);

        effect = new THREE.StereoEffect(renderer);

        scene = new THREE.Scene();

        camera = new THREE.PerspectiveCamera(90,1,0.001,700);
        camera.position.set(0,10,0);
        scene.add(camera);

        // init for the Controls
        controls = new THREE.OrbitControls(camera, element);
        controls.rotateUp(Math.PI/4);
        controls.target.set(
            camera.position.x + 0.1,
            camera.position.y,
            camera.position.z
        );
        controls.noZoom = true;
        controls.noPan = true;

        function setOrientationControls(e){
            if(!e.alpha){
                return;
            }

            controls = new THREE.DeviceOrientationControls(camera, true);
            controls.connect();
            controls.update();

            element.addEventListener('click', fullscreen, false);

            window.removeEventListener('deviceorientation', setOrientationControls, true);
        }

        window.addEventListener('deviceorientation', setOrientationControls, true);

        // Three environment setup
        var light = new THREE.HemisphereLight(0x7777777,0x000000,0.9);
        scene.add(light);

        videoTexture = new THREE.Texture(video);
        //Ground
        var groundtexture = THREE.ImageUtils.loadTexture('../images/floor.png');
        groundtexture.wrapS = THREE.RepeatWrapping;
        groundtexture.wrapT = THREE.RepeatWrapping;
        groundtexture.repeat = new THREE.Vector2(50,50);
        groundtexture.anisotropy = renderer.getMaxAnisotropy();


        var groundmaterial = new THREE.MeshPhongMaterial({
            color: 0xffffff,
            specular: 0xffffff,
            shininess: 20,
            shading: THREE.FlatShading,
            map: videoTexture
        });

        var groundgeometry = new THREE.PlaneGeometry(1000,1000);

        var groundmesh = new THREE.Mesh(groundgeometry, groundmaterial);
        groundmesh.rotation.x = -Math.PI/2;
        //scene.add(groundmesh);

        // Video
        /*
        //videoTexture = new THREE.Texture(video);

        var videomaterial = new THREE.MeshBasicMaterial({
            map: videoTexture
        });

        var videogeometry = new THREE.PlaneGeometry(200,200);

        var videomesh = new THREE.Mesh(videogeometry, videomaterial);
        videomesh.position.set(200, 100, 100);
        //scene.add(videomesh);
        */
        var bg = new THREE.Mesh(
            new THREE.PlaneGeometry(0,0,100),
            new THREE.MeshBasicMaterial({map:videoTexture})
        );
        bg.material.depthTest = false;
        bg.material.depthWrite = false;

        scene.add(bg);

        window.addEventListener('resize', resize, false);
        setTimeout(resize, 1);
    }


    function resize(){
        var width = container.offsetWidth;
        var height = container.offsetHeight;

        camera.aspect = width/height;
        camera.updateProjectionMatrix();

        renderer.setSize(width, height);
        effect.setSize(width, height);
    }

    function update(dt){
        resize();

        camera.updateProjectionMatrix();

        controls.update(dt);
    }

    function render(dt){
        effect.render(scene, camera);
    }

    function animate(t){

        if(video.readyState == video.HAVE_ENOUGH_DATA){
            videoTexture.needsUpdate = true;
        }

        renderer.autoClear = false;
        renderer.clear();
        renderer.render(scene,camera);

        requestAnimationFrame(animate);

        update(clock.getDelta());
        render(clock.getDelta());
    }

    function fullscreen(){
        if (container.requestFullscreen) {
            container.requestFullscreen();
        } else if (container.msRequestFullscreen) {
            container.msRequestFullscreen();
        } else if (container.mozRequestFullScreen) {
            container.mozRequestFullScreen();
        } else if (container.webkitRequestFullscreen) {
            container.webkitRequestFullscreen();
        }
    }

};
