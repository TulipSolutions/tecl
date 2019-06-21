/**
 * Copyright 2019 Tulip Solutions B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

window.addEventListener('DOMContentLoaded', () => {
  const selectEl = document.querySelector('#contentui-lang-select');

  const setSelected = selected => {
    selectEl.value = selected;
    document.querySelectorAll('.tab-content').forEach(tabEl => {
      if (tabEl.classList.contains('tab-' + selected)) {
        tabEl.classList.add('active');
      } else {
        tabEl.classList.remove('active');
      }
    });
  };

  selectEl.addEventListener('change', event => {
    const selected = event.target.value;
    setSelected(selected);
    localStorage.setItem('contentui-lang', selected);
  });

  setSelected(localStorage.getItem('contentui-lang'));
});
