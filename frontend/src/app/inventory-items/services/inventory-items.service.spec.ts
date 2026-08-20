import { TestBed } from '@angular/core/testing';
import { InventoryItemsService } from './inventory-items.service';
import { ApiService } from '../../core/http/api.service';
import { of, throwError } from 'rxjs';
import { InventoryItem, CreateItemFormModel } from '../models/inventory-item.model';

describe('InventoryItemsService', () => {
  let service: InventoryItemsService;
  let apiService: jasmine.SpyObj<ApiService>;

  beforeEach(() => {
    const spy = jasmine.createSpyObj('ApiService', ['get', 'post', 'patch', 'delete']);
    TestBed.configureTestingModule({
      providers: [
        InventoryItemsService,
        { provide: ApiService, useValue: spy }
      ]
    });

    service = TestBed.inject(InventoryItemsService);
    apiService = TestBed.inject(ApiService) as jasmine.SpyObj<ApiService>;
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('listItems', () => {
    it('should make GET request with default params', (done) => {
      const mockResponse = {
        content: [],
        totalElements: 0,
        totalPages: 0,
        currentPage: 0,
        pageSize: 20
      };
      apiService.get.and.returnValue(of(mockResponse));

      service.listItems().subscribe(() => {
        expect(apiService.get).toHaveBeenCalledWith(jasmine.stringContaining('/v1/inventory-items'));
        done();
      });
    });
  });

  describe('createItem', () => {
    it('should make POST request and update items', (done) => {
      const mockItem: InventoryItem = {
        id: 1,
        name: 'Test Item',
        categoryId: 1,
        locationId: 1,
        currentQuantity: 100,
        unit: 'pcs',
        lowStockThreshold: 10,
        status: 'ACTIVE',
        createdDate: '2026-08-20T00:00:00Z',
        updatedDate: '2026-08-20T00:00:00Z'
      };
      apiService.post.and.returnValue(of(mockItem));

      const formData: CreateItemFormModel = {
        name: 'Test Item',
        categoryId: 1,
        locationId: 1,
        unit: 'pcs'
      };

      service.createItem(formData).subscribe(() => {
        expect(apiService.post).toHaveBeenCalled();
        done();
      });
    });
  });

  describe('archiveItem', () => {
    it('should make POST request to archive endpoint', (done) => {
      const mockItem: InventoryItem = {
        id: 1,
        name: 'Test Item',
        categoryId: 1,
        locationId: 1,
        currentQuantity: 100,
        unit: 'pcs',
        lowStockThreshold: 10,
        status: 'ARCHIVED',
        createdDate: '2026-08-20T00:00:00Z',
        updatedDate: '2026-08-20T00:00:00Z'
      };
      apiService.post.and.returnValue(of(mockItem));

      service.archiveItem(1).subscribe(() => {
        expect(apiService.post).toHaveBeenCalledWith('/v1/inventory-items/1/archive', {});
        done();
      });
    });
  });

  describe('restoreItem', () => {
    it('should make POST request to restore endpoint', (done) => {
      const mockItem: InventoryItem = {
        id: 1,
        name: 'Test Item',
        categoryId: 1,
        locationId: 1,
        currentQuantity: 100,
        unit: 'pcs',
        lowStockThreshold: 10,
        status: 'ACTIVE',
        createdDate: '2026-08-20T00:00:00Z',
        updatedDate: '2026-08-20T00:00:00Z'
      };
      apiService.post.and.returnValue(of(mockItem));

      service.restoreItem(1).subscribe(() => {
        expect(apiService.post).toHaveBeenCalledWith('/v1/inventory-items/1/restore', {});
        done();
      });
    });
  });

  describe('deleteItem', () => {
    it('should make DELETE request', (done) => {
      apiService.delete.and.returnValue(of(void 0));

      service.deleteItem(1).subscribe(() => {
        expect(apiService.delete).toHaveBeenCalledWith('/v1/inventory-items/1');
        done();
      });
    });
  });

  describe('error handling', () => {
    it('should handle API errors', (done) => {
      const error = { status: 400, error: { message: 'Bad Request' } };
      apiService.get.and.returnValue(throwError(() => error));

      service.listItems().subscribe({
        next: () => fail('should have errored'),
        error: (err) => {
          expect(err.message).toBe('Bad Request');
          done();
        }
      });
    });
  });
});
