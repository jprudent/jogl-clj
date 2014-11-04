(ns jogl-clj.core
  (:import (javax.media.opengl GLEventListener GLCapabilities GLProfile
                               GLAutoDrawable GL2 GL GL2ES1 DebugGL2 GL3)
           (com.jogamp.opengl.util Animator FPSAnimator)
           (com.jogamp.newt.opengl GLWindow)
           (com.jogamp.newt.event WindowAdapter WindowListener)
           (javax.media.nativewindow WindowClosingProtocol$WindowClosingMode)
           (javax.media.opengl.glu GLU)
           (javax.media.opengl.fixedfunc GLMatrixFunc GLLightingFunc)))

(defn- gl-capabilities-factory []
  (doto (GLCapabilities. (GLProfile/getDefault))))

(defn- gl-window-factory [capabilities glEventListener animator]
  (doto (GLWindow/create capabilities)
    (.addGLEventListener glEventListener)
    (.addWindowListener (proxy [WindowAdapter] []
                          (window-destroyed [_]
                            (.stop animator))))
    (.setTitle "title")
    (.setSize 200 200)
    (.setUndecorated false)
    (.setPointerVisible true)
    (.setVisible true)
    (.setFullscreen false)
    (.setDefaultCloseOperation WindowClosingProtocol$WindowClosingMode/DISPOSE_ON_CLOSE)))

(defn- animator-factory [capabilities glEventListener]
  (let [animator (FPSAnimator. 1)
        window (gl-window-factory capabilities glEventListener animator)]
    (doto animator (.add window))))

(defn- call-user-def [user-def args]
  (when (not (nil? user-def))
    (apply user-def args))
  nil)

(defn- gl-event-listener-factory
  [{user-init :init user-dispose :dispose
    user-display :display user-reshape :reshape}]
  (reify GLEventListener
    (init [_ drawable]
      (call-user-def user-init [drawable]))
    (dispose [_ drawable]
      (call-user-def user-dispose [drawable]))
    (display [_ drawable]
      (call-user-def user-display [drawable]))
    (reshape [_ drawable x y width height]
      (call-user-def user-reshape [drawable x y width height]))))

;; User API

(defn start-demo [demo-def]
  (.start (animator-factory
            (gl-capabilities-factory)
            (gl-event-listener-factory demo-def))))

;; GL API

(defn gl [drawable]
  (-> drawable .getGL .getGL2 (DebugGL2.)))

(defn glVertex! [gl [x y z]]
  (.glVertex3f gl x y z))

(defn glTriangle! [gl points]
  (.glBegin gl GL/GL_TRIANGLES)
  (doseq [point points]
    (glVertex! gl point))
  (.glEnd gl))