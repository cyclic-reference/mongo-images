
import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs/Observable";

@Injectable()
export class BackendAPIService {
    constructor(private httpClient: HttpClient){}


    postImage(formData: FormData): Observable<string> {
        return this.httpClient.post('./api/image/save', formData, {
            responseType: 'text'
        });
    }

    fetchImage(_id: string): Observable<ArrayBuffer> {
        return this.httpClient.get('./api/image/get/' + _id, {
            responseType: 'arraybuffer'
        });
    }

    deleteImage(_id: string): Observable<ArrayBuffer> {
        return this.httpClient.delete('./api/image/delete/' + _id, {
            responseType: 'arraybuffer'
        });
    }
}