(ns hooks.compojure.api.sweet
  (:require [clj-kondo.hooks-api :as api]))

(def ^:private binding-option-keys
  #{:path-params :query-params :form-params :body-params :header-params})

(defn- options->map [nodes]
  (loop [remaining nodes
         opts {}
         consumed []]
    (if (and (seq remaining) (api/keyword-node? (first remaining)))
      (let [k-node (first remaining)
            v-node (second remaining)
            k (api/sexpr k-node)]
        (recur (nnext remaining)
               (assoc opts k v-node)
               (conj consumed k-node v-node)))
      {:options opts
       :consumed consumed
       :rest remaining})))

(defn- plain-symbol? [sym]
  (and (symbol? sym)
       (nil? (namespace sym))
       (not= sym ':-)
       (not= sym '&)))

(defn- collect-binding-symbols [option-node]
  (->> option-node
       api/sexpr
       (tree-seq coll? seq)
       (filter plain-symbol?)
       distinct))

(defn GET [{:keys [node]}]
  (let [[_ _ _ & more] (:children node)
        {:keys [options consumed rest]} (options->map more)
        locals (->> (select-keys options binding-option-keys)
                    vals
                    (mapcat collect-binding-symbols)
                    vec)
        option-nodes consumed
        body-nodes rest]
    (if (seq locals)
      (let [bindings (reduce (fn [acc sym]
                               (conj acc
                                     (api/token-node sym)
                                     (api/token-node nil)))
                             []
                             locals)
            let-forms (concat option-nodes body-nodes)
            let-node (api/list-node
                      (cons (api/token-node 'let)
                            (cons (api/vector-node bindings)
                                  let-forms)))]
        {:node let-node})
      (let [do-node (api/list-node
                      (cons (api/token-node 'do)
                            (concat option-nodes body-nodes)))]
        {:node do-node}))))

(defn ANY [{:keys [node]}]
  (let [[_ _ _ & more] (:children node)
        {:keys [consumed rest]} (options->map more)
        option-nodes consumed
        body-nodes rest]
    {:node
     (api/list-node
      (cons (api/token-node 'do)
            (concat option-nodes body-nodes)))}))

(defn context [{:keys [node]}]
  (let [[_ _ _ & body] (:children node)]
    {:node (api/list-node
            (cons (api/token-node 'do)
                  body))}))

(defn api [{:keys [node]}]
  (let [[_ _ & body] (:children node)]
    {:node (api/list-node
            (cons (api/token-node 'do)
                  body))}))
