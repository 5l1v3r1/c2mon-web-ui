import {IComponentOptions} from 'angular';

export class HomeComponent implements IComponentOptions {
  public templateUrl: string = '/home/home.component.html';
  public controller: Function = HomeController;
}

class HomeController {}
