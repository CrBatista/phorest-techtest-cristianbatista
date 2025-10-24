import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { BookingDataService } from './booking-data.service';
import { environment } from '../../../environments/environment';

describe('BookingDataService', () => {
  let service: BookingDataService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [BookingDataService]
    });

    service = TestBed.inject(BookingDataService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('filters appointments by client', () => {
    let results: any[] = [];
    service.getAppointmentsByClient('client-1').subscribe(value => (results = value));

    const req = httpMock.expectOne(r => r.url === `${environment.apiBaseUrl}/appointments`);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('clientId')).toBe('client-1');
    req.flush({
      content: [
        { appointmentId: 'a1', clientId: 'client-1', startTime: '2024-01-01T10:00:00Z', endTime: '2024-01-01T11:00:00Z' }
      ]
    });

    expect(results.length).toBe(1);
    expect(results[0].appointmentId).toBe('a1');
  });

  it('filters services by client', () => {
    let results: any[] = [];
    service.getServicesByClient('client-1', 50).subscribe(value => (results = value));

    const req = httpMock.expectOne(r => r.url === `${environment.apiBaseUrl}/services`);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('clientId')).toBe('client-1');
    expect(req.request.params.get('size')).toBe('50');
    req.flush({
      content: [
        { id: 's1', appointmentId: 'a1', clientId: 'client-1', name: 'Color', price: 100, loyaltyPoints: 10, performedAt: '2024-01-01T10:00:00Z' }
      ]
    });

    expect(results.length).toBe(1);
    expect(results[0].id).toBe('s1');
  });

  it('filters purchases by client', () => {
    let results: any[] = [];
    service.getPurchasesByClient('client-1').subscribe(value => (results = value));

    const req = httpMock.expectOne(r => r.url === `${environment.apiBaseUrl}/purchases`);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('clientId')).toBe('client-1');
    req.flush({
      content: [
        { id: 'p1', appointmentId: 'a1', clientId: 'client-1', name: 'Product', price: 30, loyaltyPoints: 3, performedAt: '2024-01-01T10:00:00Z' }
      ]
    });

    expect(results.length).toBe(1);
    expect(results[0].id).toBe('p1');
  });
});
