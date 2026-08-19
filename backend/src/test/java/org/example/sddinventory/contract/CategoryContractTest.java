package org.example.sddinventory.contract;

import org.junit.jupiter.api.Test;

public class CategoryContractTest {
    @Test
    public void testCreateCategoryContract() {
        // POST /api/categories
        // Request: { "name": "Electronics" }
        // Response 201: { "id": "...", "name": "Electronics", "itemCount": 0, "createdAt": "...", "updatedAt": "..." }
    }

    @Test
    public void testListCategoriesContract() {
        // GET /api/categories
        // Response 200: [ { "id": "...", "name": "Electronics", "itemCount": 0, "createdAt": "...", "updatedAt": "..." } ]
    }

    @Test
    public void testGetCategoryContract() {
        // GET /api/categories/{id}
        // Response 200: { "id": "...", "name": "Electronics", "itemCount": 0, "createdAt": "...", "updatedAt": "..." }
    }

    @Test
    public void testRenameCategoryContract() {
        // PATCH /api/categories/{id}
        // Request: { "name": "Tools" }
        // Response 200: { "id": "...", "name": "Tools", "itemCount": 0, "createdAt": "...", "updatedAt": "..." }
    }

    @Test
    public void testDeleteCategoryContract() {
        // DELETE /api/categories/{id}
        // Response 204: No Content
    }
}
