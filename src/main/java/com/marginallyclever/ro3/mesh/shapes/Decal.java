package com.marginallyclever.ro3.mesh.shapes;

import com.jogamp.opengl.GL3;
import com.marginallyclever.convenience.helpers.MathHelper;
import com.marginallyclever.ro3.mesh.Mesh;

import javax.vecmath.Vector3d;

/**
 * <p>{@link Decal} is a {@link Mesh}. It is a 1x1 quad texture on both sides. The origin is in the center of the
 * quad.</p>
 */
public class Decal extends Mesh {
	public double height = 1;
	public double width = 1;

	public Decal() {
		super();
		
		updateModel();
	}

	/**
	 * Procedurally generate a list of triangles that form a box, subdivided by some
	 * amount.
	 */
	protected void updateModel() {
		this.clear();
		this.setRenderStyle(GL3.GL_TRIANGLES);
		// model.renderStyle=GL3.GL_LINES; // set to see the wireframe

		float w = (float)width;
		float h = (float)height;

		int wParts = (int) (w / 4f) * 2;
		int hParts = (int) (h / 4f) * 2;

		Vector3d n = new Vector3d();
		Vector3d p0 = new Vector3d();
		Vector3d p1 = new Vector3d();
		Vector3d p2 = new Vector3d();
		Vector3d p3 = new Vector3d();

		// bottom
		n.set(0, 0, -1);
		p0.set(-w, h, -0.01);
		p1.set(w, h, -0.01);
		p2.set(w, -h, -0.01);
		p3.set(-w, -h, -0.01);
		addSubdividedPlane(n, p0, p1, p2, p3, wParts, hParts);

		// top
		n.set(0, 0, 1);
		p0.set(w, h, 0.01);
		p1.set(-w, h, 0.01);
		p2.set(-w, -h, 0.01);
		p3.set(w, -h, 0.01);
		addSubdividedPlane(n, p0, p1, p2, p3, wParts, hParts);
	}

	/**
	 * Subdivide a plane into triangles.
	 * 
	 * @param n      plane normal
	 * @param p0     northwest corner
	 * @param p1     northeast corner
	 * @param p2     southeast corner
	 * @param p3     southwest corner
	 * @param xParts east/west divisions
	 * @param yParts north/south divisions
	 */
	protected void addSubdividedPlane(Vector3d n,
			Vector3d p0,
			Vector3d p1,
			Vector3d p2,
			Vector3d p3,
			int xParts,
			int yParts) {
		xParts = Math.max(xParts, 1);
		yParts = Math.max(yParts, 1);

		Vector3d pA = new Vector3d();
		Vector3d pB = new Vector3d();
		Vector3d pC = new Vector3d();
		Vector3d pD = new Vector3d();
		Vector3d pE = new Vector3d();
		Vector3d pF = new Vector3d();
		Vector3d pG = new Vector3d();
		Vector3d pH = new Vector3d();

		for (int x = 0; x < xParts; x++) {
			pA.set(MathHelper.interpolate(p0, p1, (double) (x) / (double) xParts));
			pB.set(MathHelper.interpolate(p0, p1, (double) (x + 1) / (double) xParts));
			pC.set(MathHelper.interpolate(p3, p2, (double) (x) / (double) xParts));
			pD.set(MathHelper.interpolate(p3, p2, (double) (x + 1) / (double) xParts));

			for (int y = 0; y < yParts; y++) {
				pE.set(MathHelper.interpolate(pA, pC, (double) (y) / (double) yParts));
				pF.set(MathHelper.interpolate(pB, pD, (double) (y) / (double) yParts));
				pG.set(MathHelper.interpolate(pA, pC, (double) (y + 1) / (double) yParts));
				pH.set(MathHelper.interpolate(pB, pD, (double) (y + 1) / (double) yParts));

				if (this.getRenderStyle() == GL3.GL_TRIANGLES) {
					for (int j = 0; j < 6; ++j) {
						this.addNormal((float) n.x, (float) n.y, (float) n.z);
						this.addColor(1, 1, 1, 1);
					}

					this.addVertex((float) pE.x, (float) pE.y, (float) pE.z);
					this.addVertex((float) pF.x, (float) pF.y, (float) pF.z);
					this.addVertex((float) pH.x, (float) pH.y, (float) pH.z);

					this.addVertex((float) pE.x, (float) pE.y, (float) pE.z);
					this.addVertex((float) pH.x, (float) pH.y, (float) pH.z);
					this.addVertex((float) pG.x, (float) pG.y, (float) pG.z);
				} else if (this.getRenderStyle() == GL3.GL_LINES) {
					for (int j = 0; j < 8; ++j) {
						this.addNormal((float) n.x, (float) n.y, (float) n.z);
						this.addColor(1, 1, 1, 1);
					}

					this.addVertex((float) pF.x, (float) pF.y, (float) pF.z);
					this.addVertex((float) pH.x, (float) pH.y, (float) pH.z);

					this.addVertex((float) pH.x, (float) pH.y, (float) pH.z);
					this.addVertex((float) pE.x, (float) pE.y, (float) pE.z);

					this.addVertex((float) pH.x, (float) pH.y, (float) pH.z);
					this.addVertex((float) pG.x, (float) pG.y, (float) pG.z);

					this.addVertex((float) pG.x, (float) pG.y, (float) pG.z);
					this.addVertex((float) pE.x, (float) pE.y, (float) pE.z);
				}
			}
		}
	}
/*
	@Override
	public JSONObject toJSON(SerializationContext context) {
		JSONObject jo = super.toJSON(context);
		jo.put("width", width.toJSON(context));
		jo.put("height", height.toJSON(context));

		return jo;
	}

	@Override
	public void parseJSON(JSONObject jo, SerializationContext context) throws JSONException {
		super.parseJSON(jo, context);
		width.parseJSON(jo.getJSONObject("width"), context);
		height.parseJSON(jo.getJSONObject("height"), context);
	}
 */
}
