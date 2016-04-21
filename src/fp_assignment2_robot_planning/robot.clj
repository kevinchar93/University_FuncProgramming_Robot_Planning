(ns fp-assignment2-robot-planning.robot
  (use [fp-assignment2-robot-planning.core])
  (require [clojure.pprint :as pp])
  (require [clojure.data.priority-map :as pm]))

;-------------------------------------------------------------------------------
; define a record to represent a parcel
(defrecord Parcel [origin-room            ; the room the parcel is originally in
                   dest-room              ; the destination the parcel is going to
                   content                ; the parcel's content
                   delivered])            ; whether the parcel has been delevered or not


;-------------------------------------------------------------------------------
; define a record to represent a robot
(defrecord Robot [graph          ; the graph which represents the building plan
                  curr-pos       ; the current position of the robot
                  parcels           ; the parcels the robot is carrying
                  parcel-map        ; the information about where parcels are in the building
                  curr-path      ; the path the the robot is on currently
                  curr-path-idx])   ; the index of 'current-pos' in 'current-path'




(defn pn [arg]
  (pp/pprint arg))




;-------------------------------------------------------------------------------
(defn pickup-parcels [robot]
  "Picks up all parcels at the robot's current location, updates parcel list and parcel map accordingly"
  (let [robot-pos (:curr-pos robot)                        ; get the robot's current positon
        parcel-map (:parcel-map robot)                     ; get the robot's parcel-map
        parcels-pos (robot-pos parcel-map)                 ; use robot-pos to get all the parcels in the map at that position
        
        undelivered (parcels-by-deliv false        ; get all the undelivered parcels at the current position
                                              parcels-pos)
        remaining (parcels-by-deliv true           ; get all the remaining parcels that have been delivered already
                                              parcels-pos)
        parcel-map* (assoc parcel-map robot-pos remaining) ; create a parcel map with collected parcels in 'undelivered' removed
        parcels (:parcels robot)                           ; get the robot's original parcel list
        
        robot+ (assoc robot :parcels (apply conj           ; update the original 'robot' parcel list (parcels) with the list... 
                                            parcels        ; of undelivered parcels in 'undelivered'
                                            undelivered))   
        
        robot++ (assoc robot+ :parcel-map parcel-map*)]    ; update the modified 'robot+' parcel-map to change it to the...
                                                           ; updated version 'parcel-map*'
                                                           
    robot++))                                              ; return the newly updated robot



;-------------------------------------------------------------------------------
#_(defn dropoff-parcels [robot]
    (let ))



;-------------------------------------------------------------------------------
(defn cost-to-parcel [flag pos parcel graph]
  "Calculate the cost of travelling from 'pos' to destination of 'parcel'"
  (let [dest (flag parcel)          ; get the destination of the parcel
        path (plan-path graph pos dest)   ; plan a path from 'pos' to the parcels destination
        dist (path-cost graph path)]      ; get the cost of the path
    dist))



;-------------------------------------------------------------------------------
(defn weigh-parcels [flag pos parcels graph]
  "Map the parcels to thier distance from 'pos'"
  (let [weight-map1 (for [p parcels                       ; create first stage of the weight map, putting parcels into...
                          :let [dist (cost-to-parcel flag ; seqs with the cost of a path from 'pos' to 'p'
                                                     pos
                                                     p 
                                                     graph)]]
                      [p dist])
        weight-map2 (apply pm/priority-map                ; take the first stage of the weight map and put it into a...
                           (flatten weight-map1))]        ; priority map that sorts based on vals
    weight-map2))


(pn (plan-route ROBOT))
;-------------------------------------------------------------------------------
(defn plan-route [robot]
  (loop [pos (:curr-pos robot)                         ; init as current position of the robot (on recur will be end of total-path)
         parcels (parcels-by-deliv false               ; init the parcels list to have all undelivered parcels
                                  (:parcel-map robot))
         total-path (conj [] (:curr-pos robot))        ; init the total-path's first node, which the robot's current positon
         to-deliver nil]                               ; to-deliver list of parcels begins empty
    
    (let [graph (:graph robot)                         ; get the graph in the robot that represents the map of the building
          par-weight-map (weigh-parcels :origin-room   ; map the parcels to the cost to collect them, use as a "to collect" list
                                        pos        
                                        parcels
                                        graph) 
          [curr-parcel _ ] (first par-weight-map)      ; get the parcel closest to the current position (pos)
          dest (:dest-room curr-parcel)                ; get the destination for the current parcel
          plan (plan-path graph pos dest)              ; plan a path from the current position in the plan to dest
          r-plan (subvec plan 1)                       ; getting the plan minus the first node in it
          
          total-path* (if (not (empty? r-plan))        ; append r-plan to the total-path if there is nothing in r-plan...
                        (apply conj                    ; then the total-path remains unchanged
                              total-path 
                              r-plan)
                        total-path)
          pos* (last total-path*)                      ; update the current position to be at the end of the generated path
          par-weight-map* (dissoc par-weight-map       ; get the new parcel map with the current parcel removed from it
                                  curr-parcel)
          parcels* (keys par-weight-map*)]             ; get the new list of parcels from par-weight-map*
      parcels*)))


(defn plan-deliv-path [])



(def exA (plan-path building :a2 :d2))
(def exB (plan-path building :ts :c107))

(def exC (apply conj [:r125] []))
(def exD (apply conj exC exB))
(pn exC)
(pn exD)

;-------------------------------------------------------------------------------
; define where all parcels currently are and where ther are to be delivered
;                        |origin|dest|content|delivered|
(def all-parcels [(Parcel. :r123 :r125 "dog" false)
                  (Parcel. :r125 :r127 "fish" false)
                  (Parcel. :r125 :r115 "lion" false)
                  (Parcel. :r125 :r113 "zeebra" false)
                  (Parcel. :r131 :r111 "horse" true)
                  (Parcel. :r131 :r101 "cat" true)
                  (Parcel. :r125 :r103 "mouse" true)
                  (Parcel. :r129 :r107 "cow" true)])



;-------------------------------------------------------------------------------
; define the initial position of the robot
(def initial-pos :d3 )



;-------------------------------------------------------------------------------
; init information about the simulated robot
(def ROBOT (Robot. building
                   initial-pos                         ; set initial position of robot, defined above
                   nil                                 ; parcels list starts out empty
                   (add-parcels-to-reg {} all-parcels) ; add all the parcels in 'all-parcels' to the robot's parcel map
                   []                                  ; robot's path is initially empty
                   nil))                               ; not on a path so current index initialised to nil


