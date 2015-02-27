
var particles = [];

function Vector(x,y){
    this.x = x || 0;
    this.y = y || 0;
}

Vector.prototype.add = function(vector){
    this.x += vector.x;
    this.y += vector.y;
};

Vector.prototype.getMagnitude = function(){
    return Math.sqrt(this.x * this.x + this.y * this.y);
};

Vector.prototype.getAngle = function(){
    return Math.atan2(this.y, this.x);
};

Vector.fromAngle = function(angle, magnitude){
    return new Vector(magnitude * Math.cos(angle), magnitude * Math.sin(angle));
};

function Particle(id, target, point, velocity, acceleration, color){
    this.id = id;
    this.targetpost = target|| new Vector(0,0);
    this.position = point || new Vector(0,0);
    this.velocity = velocity || new Vector(0,0);
    this.acceleration = acceleration || new Vector(0,0);
    this.drawColor = color;
}

Particle.prototype.move = function(){
    this.velocity.add(this.acceleration);
    this.position.add(this.velocity);
};

Particle.prototype.click = function(){};

var canvas = document.querySelector('canvas');
var ctx = canvas.getContext('2d');

canvas.width = window.innerWidth;
canvas.height = window.innerHeight;

function loop(){
    clear();
    update();
    draw();
    queue();
}

function clear(){
    ctx.clearRect(0,0,canvas.width, canvas.height);
}

function queue(){
    window.requestAnimationFrame(loop);
}

function update(){

}

function draw(){

}

loop();