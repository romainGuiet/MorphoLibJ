/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2017 INRA.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */
package inra.ijpb.data.border;

import ij.process.ImageProcessor;

/**
 * Returns either image pixel when position is inside image bounds, 
 * or a constant value when position is outside of image bounds.
 * @author David Legland
 *
 */
public class ConstantBorder implements BorderManager
{

	ImageProcessor image;
	float value;

	/**
	 * Creates a new Constant Border Manager
	 * 
	 * @param image
	 *            the image to expand
	 * @param value
	 *            the value used to expand the borders.
	 */
	public ConstantBorder(ImageProcessor image, float value)
	{
		this.image = image;
		this.value = value;
	}

	/**
	 * Returns either image pixel when position is inside image bounds, or a
	 * constant value when position is outside of image bounds.
	 * 
	 * @see inra.ijpb.data.border.BorderManager#get(int, int)
	 */
	@Override
	public int get(int x, int y)
	{
		if (x < 0)
			return (int) this.value;
		if (y < 0)
			return (int) this.value;
		if (x >= this.image.getWidth())
			return (int) this.value;
		if (y >= this.image.getHeight())
			return (int) this.value;
		return this.image.get(x, y);
	}

	/**
	 * Returns either image pixel when position is inside image bounds, or a
	 * constant value when position is outside of image bounds.
	 * 
	 * @see inra.ijpb.data.border.BorderManager#getf(int, int)
	 */
	@Override
	public float getf(int x, int y)
	{
		if (x < 0)
			return this.value;
		if (y < 0)
			return this.value;
		if (x >= this.image.getWidth())
			return this.value;
		if (y >= this.image.getHeight())
			return this.value;
		return this.image.getf(x, y);
	}

}
