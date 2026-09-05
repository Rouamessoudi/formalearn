import { inject } from '@angular/core';
import { ResolveFn } from '@angular/router';
import { Formation, FormationApi } from '../api/formation.api';

export const formationResolver: ResolveFn<Formation> = route => {
  return inject(FormationApi).get(Number(route.paramMap.get('id')));
};
