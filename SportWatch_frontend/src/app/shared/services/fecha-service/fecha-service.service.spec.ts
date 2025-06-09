import { TestBed } from '@angular/core/testing';

import { FechaServiceService } from './fecha-service.service';

describe('FechaServiceService', () => {
  let service: FechaServiceService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(FechaServiceService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
