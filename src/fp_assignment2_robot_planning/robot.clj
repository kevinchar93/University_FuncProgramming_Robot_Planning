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
                  parcels        ; the parcels the robot is carrying
                  parcel-map     ; the information about where parcels are in the building
                  curr-path      ; the path the the robot is on currently
                  deliveries     ; map containing which rooms parcels are to be delivered to and how many
                  collections    ; map containing which rooms parcels are to be collected from and how many
                  ]) 




(defn pn [arg]
  (pp/pprint arg))




;-------------------------------------------------------------------------------
(defn pickup-parcels [robot]
  "Picks up all parcels at the robot's current location, updates parcel list and parcel map accordingly"
  (let [robot-pos (:curr-pos robot)                         ; get the robot's current positon
        parcel-map (:parcel-map robot)                      ; get the robot's parcel-map
        parcels-pos (robot-pos parcel-map)                  ; use robot-pos to get all the parcels in the map at that position
        
        undelivered (parcels-by-deliv false                 ; get all the undelivered parcels at the current position
                                              parcels-pos)
        remaining (parcels-by-deliv true                    ; get all the remaining parcels that have been delivered already
                                              parcels-pos)
        parcel-map* (assoc parcel-map robot-pos remaining)  ; create a parcel map with collected parcels in 'undelivered' removed
        parcels (:parcels robot)                            ; get the robot's original parcel list
        
        robot+ (assoc robot :parcels (apply conj            ; update the original 'robot' parcel list (parcels) with the list... 
                                            parcels         ; of undelivered parcels in 'undelivered'
                                            undelivered))   
        
        robot++ (assoc robot+ :parcel-map parcel-map*)]     ; update the modified 'robot+' parcel-map to change it to the...
                                                            ; updated version 'parcel-map*'
                                                           
    robot++))                                               ; return the newly updated robot




;-------------------------------------------------------------------------------
#_(defn dropoff-parcels [robot]
    (let ))



;-------------------------------------------------------------------------------
(defn cost-to-parcel [flag pos parcel graph]
  "Calculate the cost of travelling from 'pos' to destination of 'parcel'"
  (let [dest (flag parcel)                ; get the destination of the parcel
        path (plan-path graph pos dest)   ; plan a path from 'pos' to the parcels destination
        dist (path-cost graph path)]      ; get the cost of the path
    dist))




;-------------------------------------------------------------------------------
(defn weigh-parcels [flag pos parcels graph]
  "Map the parcels to thier distance from 'pos'"
  (let [weight-map1 (for [p parcels                           ; create first stage of the weight map, putting parcels into...
                          :let [dist (cost-to-parcel flag     ; paired vectors with the cost of a path from 'pos' to 'p'
                                                     pos
                                                     p 
                                                     graph)]]
                      [p dist])
        weight-map2 (apply pm/priority-map                    ; take the first stage of the weight map and put it into a...
                           (flatten weight-map1))]            ; priority map that sorts based on vals
    weight-map2))




;-------------------------------------------------------------------------------
(defn plan-collection-path [init-pos parcel-map graph]
  "Plan a path the robot will follow to collect parcels.
 
   Returns [collection-path  collection-points-map  parcel-delivery-list]"
  
  (loop [pos init-pos                                   ; init as current position of the robot (on recur will be end of total-path)
         parcels (parcels-by-deliv false                ; init the parcels list to have all undelivered parcels
                                  parcel-map)
         total-path (conj [] init-pos)                  ; init the total-path's first node, which the robot's current positon
         coll-points {}                                 ; map stores the rooms parcels are to be collected from, init to empty
         to-deliver nil]                                ; to-deliver list of parcels begins empty
    
    (if parcels                                         ; keep looping over list of parcels until exhausted 
      (let [par-weight-map (weigh-parcels :origin-room  ; map the parcels to the cost to collect them, use as a "to collect" list
                                          pos        
                                          parcels
                                          graph) 
           [curr-parcel _ ] (first par-weight-map)      ; get the parcel closest to the current position (pos)
           loc (:origin-room curr-parcel)               ; get the pick-up location for the current parcel
           
           count (get coll-points loc)                  ; get the ammount of parcels that are to be collected at loc
           coll-points* (if count                       ; incremet the ammout of parcels to collect at loc
                          (assoc coll-points
                                 loc
                                 (inc count))
                          (assoc coll-points loc 1))
           
           plan (plan-path graph pos loc)               ; plan a path from the current position in the plan to loc
           r-plan (subvec plan 1)                       ; getting the plan minus the first node in it
          
           total-path* (if (not (empty? r-plan))        ; append r-plan to the total-path if there is nothing in r-plan...
                         (apply conj                    ; then the total-path remains unchanged
                               total-path 
                               r-plan)
                         total-path)
           pos* (last total-path*)                      ; update the current position to be at the end of the generated path
           par-weight-map* (dissoc par-weight-map       ; get the new parcel map with the current parcel removed from it
                                   curr-parcel)
           parcels* (keys par-weight-map*)              ; get the new list of parcels from par-weight-map*
           to-deliver* (conj to-deliver curr-parcel)]   ; add the current parcel to the 'to-deliver' list
       
        (recur pos*                                     ; recur and rebind new arguments for position, parcel list the total...
               parcels*                                 ; total generated path, collection points and the to deliver list
               total-path*
               coll-points*
               to-deliver*))
      
      [total-path                                       ; return a vector containing the collection path, a map of the locations...
       coll-points                                      ; the parcels are to be collected from and the ammout to collect from...
       to-deliver])))                                   ; said location and the list of parcels to deliver




;-------------------------------------------------------------------------------
(defn plan-delivery-path [to-deliver partial-path graph]
  "Continue a planned collection path by taking a list of deliveries to be made 
   and planning a path to make these deliveries.

   Returns [full-collect-deliver-path  delivery-points]"
  
  (loop [pos (last partial-path)                        ; position begins from where other partial path ended
         
         parcels to-deliver                             ; parcel list that is iterated over is list of parcels to deliver...
                                                        ; created in a previos stage
                                                        
         deliv-points {}                                ; map stores the rooms parcels are to be delivered to, init to empty
         
         total-path partial-path]                       ; the total-path is initialised from the partial path, nodes are..
                                                        ; added to this
    
    (if parcels                                         ; keep looping over list of parcels until exhausted 
      (let [par-weight-map (weigh-parcels :dest-room    ; map the parcels to the cost to deliver them, use as a "to deliver" list
                                          pos        
                                          parcels
                                          graph) 
           [curr-parcel _ ] (first par-weight-map)      ; get the parcel closest to the current position (pos)
           dest (:dest-room curr-parcel)                ; get the destination for the current parcel
           
           count (get deliv-points dest)                ; get the ammount of parcels that are to be delivered at dest
           deliv-points* (if count                      ; incremet the ammout of parcels to collect at dest
                           (assoc deliv-points
                                  dest
                                  (inc count))
                           (assoc deliv-points dest 1))
           
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
           parcels* (keys par-weight-map*)              ; get the new list of parcels from par-weight-map*
           ] 
       
        (recur pos*                                     ; recur and rebind new arguments for position, parcel list...
               parcels*                                 ; and the total generated path
               deliv-points*
               total-path*))
      
      [total-path                                       ; return the total planned path and the map containing the locations...
       deliv-points])))                                 ; the parcels are to be delivered to and the ammount to deliver to...
                                                        ; said location



                                                        
;-------------------------------------------------------------------------------     
(defn plan-full-route [robot]
  "Plans a full route the robot can uses to collect and deliver all 
   the parcels in its parcel map"
  
  (let [r-graph (:graph robot)                        ; get the graph the robot uses as a map
        r-pos (:curr-pos robot)                       ; get the current position of the robot
        r-parcel-map (:parcel-map robot)              ; get the parcet map from the robot

        [coll-path                                    ; destructure result from 'plan-collection-path'
         coll-points
         to-deliver
         ] (plan-collection-path r-pos                ; use details from the robot to plan a parcel collection path
                                 r-parcel-map
                                 r-graph)
        [full-path                                    ; destructure result from 'plan-collection-path'
         deliv-points
         ] (plan-delivery-path to-deliver             ; use the collection path and the list of parcels in 'to-deliver'...
                               coll-path              ; to create a fully planned path the robot can use to coll/deliv
                               r-graph)
        
        robot (assoc robot :curr-path full-path)      ; update the robot's current path to newly planned path
        robot (assoc robot :deliveries deliv-points)  ; update the robot's deliveries map to delivery points gathered
        robot (assoc robot :collections coll-points)  ; update the robot's collections map to collection points gathered
        ]
    robot))





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

; parcel lists for each task in the assignment brief
(def task1 [(Parcel. :)])



;-------------------------------------------------------------------------------
; define the initial position of the robot
(def initial-pos :c101 )


;-------------------------------------------------------------------------------
; init information about the simulated robot
(def ROBOT (Robot. building
                   initial-pos                         ; set initial position of robot, defined above
                   nil                                 ; parcels list starts out empty
                   (add-parcels-to-reg {} all-parcels) ; add all the parcels in 'all-parcels' to the robot's parcel map
                   []                                  ; robot's path is initially empty
                   {}                                  ; map for deliveries is initially empty
                   {}))                                ; map for collections is initially empty


