package models;

import java.math.BigDecimal;
import java.util.List;

public class Page<T> {

	public final int pageSize;
	public final int totalRowCount;
	public final int pageIndex;
	public final List<T> list;

	public Page(List<T> data, int total, int page, int pageSize) {
		this.list = data;
		this.totalRowCount = total;
		this.pageIndex = page;
		this.pageSize = pageSize;
	}

	public boolean hasPrev() {
		return pageIndex > 1;
	}

	public boolean hasNext() {
		if (totalRowCount % 10 == 0) {
			return (totalRowCount / pageSize) > pageIndex;
		} else {
			return (totalRowCount / pageSize) >= pageIndex;
		}

	}

	public String getDisplayXtoYofZ() {
		int start = ((pageIndex - 1) * pageSize + 1);
		int end = start + Math.min(pageSize, list.size()) - 1;
		return start + " to " + end + " of " + totalRowCount;
	}

	public static int adjustPage(int page, int total, int pageSize) {
		if (page <= 0) { // if page is 0 or less, get last page.
			if (total < pageSize) {
				page = 1;
			} else {
				BigDecimal bd = new BigDecimal(total).divide(new BigDecimal(pageSize)).setScale(0, BigDecimal.ROUND_HALF_UP);
				page = bd.intValue();
			}
		}

		return page;
	}
}