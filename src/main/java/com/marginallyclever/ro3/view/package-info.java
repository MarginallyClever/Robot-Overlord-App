/*
 <p>Suppose there are one or more classes that provide different views of a model, in the sense of a
 <a href="https://en.wikipedia.org/wiki/Model%E2%80%93view%E2%80%93controller">model-view-controller design
 pattern</a>.</p>
 <p>When class A provides a {@link com.marginallyclever.ro3.view.View} for model class B, A should be annotated
 <code>View(of=B.class)</code>.  A must also implement the {@link com.marginallyclever.ro3.view.ViewProvider}
 interface.  {@link com.marginallyclever.ro3.view.ViewProvider} then empowers other systems to locate and
 build the Swing GUI for A.</p>
 <p>Annotating a {@link com.marginallyclever.ro3.view.View} and failing to implement the
 {@link com.marginallyclever.ro3.view.ViewProvider} interface will cause a test to fail.</p>

 <p>This system allows for modular and dynamic view creation for various data types in a Swing-based application.</p>
 */
package com.marginallyclever.ro3.view;
