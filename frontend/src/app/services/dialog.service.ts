import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

export interface DialogConfig {
  title?: string;
  data?: any;
  width?: string;
}

export interface DialogRef<T> {
  close(result?: T): void;
  afterClosed(): Observable<T | undefined>;
}

@Injectable({
  providedIn: 'root'
})
export class DialogService {
  private dialogsSubject = new BehaviorSubject<DialogRef<any>[]>([]);
  dialogs$ = this.dialogsSubject.asObservable();

  open<T>(component: any, config?: DialogConfig): DialogRef<T> {
    const resultSubject = new BehaviorSubject<T | undefined>(undefined);
    const dialogRef: DialogRef<T> = {
      close: (result?: T) => {
        resultSubject.next(result);
        resultSubject.complete();
        const dialogs = this.dialogsSubject.getValue();
        this.dialogsSubject.next(dialogs.filter(d => d !== dialogRef));
      },
      afterClosed: () => resultSubject.asObservable()
    };

    const dialogs = this.dialogsSubject.getValue();
    this.dialogsSubject.next([...dialogs, dialogRef]);

    return dialogRef;
  }

  closeAll(): void {
    this.dialogsSubject.next([]);
  }
}
