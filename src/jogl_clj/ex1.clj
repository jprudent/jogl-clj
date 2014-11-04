(ns jogl-clj.ex1
  (:use [jogl-clj.core :refer :all])
  (:import (javax.media.opengl GLEventListener GLCapabilities GLProfile
                               GLAutoDrawable GL2 GL GL2ES1 DebugGL2 GL3)

           (com.jogamp.newt.opengl GLWindow)
           (com.jogamp.newt.event WindowAdapter WindowListener)
           (javax.media.nativewindow WindowClosingProtocol$WindowClosingMode)
           (javax.media.opengl.glu GLU)
           (javax.media.opengl.fixedfunc GLMatrixFunc GLLightingFunc)))

(def state (atom {:zoom -2}))

(defn zoom! [s f] (swap! s #(update-in % [:zoom] f)))

(def glu (GLU.))


(def demo-def
  {:init
    (fn [drawable]
      (doto (gl drawable)
        (.glClearColor 0 0 0 0)
        (.glClearDepth 1)
        (.glEnable GL/GL_DEPTH_TEST)
        (.glDepthFunc GL/GL_LEQUAL)
        (.glHint GL2ES1/GL_PERSPECTIVE_CORRECTION_HINT, GL/GL_NICEST)
        (.glShadeModel GLLightingFunc/GL_SMOOTH)))

   :display
    (fn [drawable]
      (doto (gl drawable)
        (.glClear (bit-or GL/GL_COLOR_BUFFER_BIT GL/GL_DEPTH_BUFFER_BIT))
        (.glLoadIdentity)
        (.glTranslatef 0 0 (:zoom (zoom! state identity)))
        (glTriangle! [[0 1 0] [-1 -1 0] [1 -1 0]])))

   :reshape
    (fn [drawable _ _ width height]
      (let [height (max 1 height)
            gl (gl drawable)]
        (doto gl
          (.glViewport 0 0 width height)
          (.glMatrixMode GLMatrixFunc/GL_PROJECTION)
          (.glLoadIdentity))
        (.gluPerspective glu 45. (double (/ width height)) 0.1 100.)
        (doto gl (.glMatrixMode GLMatrixFunc/GL_MODELVIEW)
                 (.glLoadIdentity))))})

(start-demo demo-def)