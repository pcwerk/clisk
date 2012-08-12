(ns 
  ^{:author "mikera"
    :doc "Library of clisk patterns generators"}
  clisk.patterns
  
  "Patterns and pattern generators"
  (:use [clisk util node functions])
  (:import java.awt.image.BufferedImage)
  (:import clisk.noise.Perlin)
  (:import clisk.noise.Simplex)
  (:import clisk.generator.Voronoi2D)
)


(def perlin-noise 
  "Standard 4-dimensional scalar perlin noise in range [0..1]"
  '(clisk.noise.Perlin/noise x y z t))

(def perlin-snoise
  "4-dimensional scalar perlin noise standardised with mean zero, range [-1..1]"
  '(clisk.noise.Perlin/snoise x y z t))

(def simplex-noise 
  "Standard 4-dimensional scalar perlin noise in range [0..1]"
  '(clisk.noise.Simplex/noise x y z t))

(def simplex-snoise
  "4-dimensional scalar simplex noise standardised with mean zero, range [-1..1]"
  '(clisk.noise.Simplex/snoise x y z t))

(defn tile
  "Tiles a pattern in the range [0..1,0..1]"
  ([pattern]
    (warp vfrac pattern)))

(def grain
  "Pattern returning a unique vector in [0..1)^4 range value for every point in 4D space"
  vector-hash)

(def noise
  simplex-noise)

(def snoise
  simplex-snoise)

(defn make-multi-fractal 
  "Creates a multi-fractal function from a given source function with additional parameters"
  ([function & {:keys [octaves lacunarity gain scale]
                :or {octaves 8
                     lacunarity 2.0
                     gain 0.5
                     scale 0.5}}]
    (apply v+
      (for [octave (range 0 octaves)]        
        (warp 
          (v* pos (Math/pow lacunarity octave))
          (v* (* scale (Math/pow gain octave)) function))))))



(def hash-cubes 
    "4 dimensional randomly coloured unit hypercubes filling space"
    (warp vfloor grain))

(def colour-cubes 
    "4 dimensional randomly coloured unit hypercubes filling space"
    (warp vfloor grain))

(def vnoise 
  "4 dimensional vector perlin noise in range [0..1]^4"
  (vector-offsets noise))

(def vsnoise 
  "4 dimensional vector standardised perlin noise in range [-1..1]^4"
  (vector-offsets snoise))

(defn swirl
  "Swirls a function around the origin with a given rate"
  ([function]
    (swirl 1.0 function))
  ([rate function]
    (rotate (v* rate (vsqrt length) ) function)))

(def plasma 
  "4 dimensional plasma, in range [0..1]"
  (make-multi-fractal noise))

(def turbulence
  "Classic Perlin turbulence in one dimension"
  (make-multi-fractal (vabs snoise)))

(def vturbulence
  "Classic Perlin turbulence in 4 dimensions"
  (make-multi-fractal (vabs vsnoise)))

(def vplasma 
  "4 dimensional vector plasma in range [0..1]^4"
  (vector-offsets plasma))

(defn turbulate
  "Adds random turbulence to a pattern according to a perlin noise offset"
  ([factor func]
    (offset (v* factor turbulence) func)))

(defmethod clojure.core/print-dup java.awt.image.BufferedImage
  [^BufferedImage bi writer]
  (print-dup "[BufferedImage]" writer))

(defn checker 
  "Checker pattern in (x,y) space, with 2*2 grid in [0..1,0..1] range"
  ([a b]
    (vif '(clojure.core/* 
            (clojure.core/- (clisk.functions/frac x) 0.5)
            (clojure.core/- (clisk.functions/frac y) 0.5))
         a
         b)))

(defn globe
  "Creates a globe, returning the value of the function called 
   on the surface of a unit sphere. 

   (globe) alone produces z values that Can be used as a hight map"
  ([]
    (globe z 0.0))
  ([function]
    (globe function 0.0))
  ([function background]
    (vif 
      (v- 1.0 (length [x y]))
      (warp [x y `(Math/sqrt (- 1.0 ~(:code (dot [x y] [x y]))))] function )
      background)))

(defn ^clisk.generator.Voronoi2D cast-voronoi [x]
  x)

(defn voronoi-points
  ([& {:keys [points] 
       :or {points 16}}]
    (let [v-sym (gensym "voronoi")
          voronoi (clisk.generator.Voronoi2D. (int points))
          obj-map {v-sym voronoi}] 
      (vector-node
          (code-node `(.nearestX (cast-voronoi ~v-sym) ~'x ~'y) 
                     :objects obj-map)
          (code-node `(.nearestY (cast-voronoi ~v-sym) ~'x ~'y) 
                     :objects obj-map)))))

