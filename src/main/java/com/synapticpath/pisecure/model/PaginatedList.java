/* Copyright (C) 2016 synapticpath.com - All Rights Reserved

 This file is part of Pi-Secure.

    Pi-Secure is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Pi-Secure is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Pi-Secure.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.synapticpath.pisecure.model;

import java.util.Iterator;
import java.util.List;

import com.synapticpath.pisecure.Exportable;

/**
 * Allows export of paginated data.
 * 
 * @author jmarvan@synapticpath.com
 *
 * @param <T>
 */
public class PaginatedList<T extends Exportable> {

	private List<T> items;

	private int offset;

	private int pageSize;

	private int total;

	public List<T> getItems() {
		return items;
	}

	public void setItems(List<T> items) {
		this.items = items;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public String toJson() {
		StringBuilder sb = new StringBuilder("{\r\n");
		sb.append("\"offset\":").append(offset).append(",\r\n");
		sb.append("\"page\":").append(offset > 0 ? offset/pageSize +1: 1).append(",\r\n");
		sb.append("\"pageSize\":").append(pageSize).append(",\r\n");
		sb.append("\"total\":").append(total).append(",\r\n");
		sb.append("\"totalPages\":").append(total % pageSize > 0 ? total/pageSize+1 : total/pageSize).append(",\r\n");
		sb.append("\"next\":").append(offset+pageSize >= total ? offset : offset+pageSize).append(",\r\n");
		sb.append("\"previous\":").append(offset-pageSize < 0 ? 0 : offset-pageSize).append(",\r\n");
		sb.append("\"data\":[\r\n");

		Iterator<T> iter = items.iterator();
		while (iter.hasNext()) {
			sb.append(iter.next().toJson());
			if (iter.hasNext()) {
				sb.append(",\r\n");
			}
		}
		sb.append("]}");
		return sb.toString();
	}

	public static <T extends Exportable> PaginatedList<T> create(int total, int offset, int pageSize, List<T> items) {

		PaginatedList<T> pl = new PaginatedList<>();
		pl.setItems(items);
		pl.setTotal(total);
		pl.setOffset(offset);
		pl.setPageSize(pageSize);

		return pl;
	}

}
