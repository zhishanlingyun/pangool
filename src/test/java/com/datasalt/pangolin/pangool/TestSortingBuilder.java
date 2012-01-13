package com.datasalt.pangolin.pangool;

import junit.framework.Assert;

import org.junit.Test;

import com.datasalt.pangolin.grouper.io.tuple.ITuple.InvalidFieldException;
import com.datasalt.pangolin.pangool.SortCriteria.SortOrder;

public class TestSortingBuilder {

	@Test
	public void testBuildSorting() throws InvalidFieldException {
		SortingBuilder builder = new SortingBuilder();
		Sorting sorting = builder.add("url", SortOrder.ASC).add("date", SortOrder.DESC)
			.secondarySort(1).add("name", SortOrder.ASC).add("surname", SortOrder.DESC)
			.secondarySort(2).add("taste", SortOrder.ASC)
			.buildSorting();
		Assert.assertNotNull(sorting);
		Assert.assertEquals(SortOrder.ASC, sorting.getSortCriteria().getSortElementByFieldName("url").getSortOrder());
		Assert.assertEquals(SortOrder.DESC, sorting.getSortCriteria().getSortElementByFieldName("date").getSortOrder());
		Assert.assertEquals(SortOrder.ASC, sorting.getSecondarySortCriteriaByName(1).getSortElementByFieldName("name").getSortOrder());
		Assert.assertEquals(SortOrder.DESC, sorting.getSecondarySortCriteriaByName(1).getSortElementByFieldName("surname").getSortOrder());
		Assert.assertEquals(SortOrder.ASC, sorting.getSecondarySortCriteriaByName(2).getSortElementByFieldName("taste").getSortOrder());		
	}
}