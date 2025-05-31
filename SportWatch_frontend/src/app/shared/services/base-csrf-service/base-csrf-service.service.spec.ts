import { TestBed } from '@angular/core/testing';

import { BaseCsrfServiceService } from './base-csrf-service.service';

describe('BaseCsrfServiceService', () => {
  let service: BaseCsrfServiceService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(BaseCsrfServiceService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
