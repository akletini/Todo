Date.prototype.toDateInputValue = function () {
    var local = new Date(this);
    local.setMinutes(this.getMinutes() - this.getTimezoneOffset());
    return local.toJSON().slice(0, 10);
  };

  $(document).ready(function setDatefield() {
    document.getElementById("datepicker").defaultValue =
      new Date().toDateInputValue();
  });

  function sortList(selectedIndex) {
    if (selectedIndex.value == 1) {
      window.location.replace("/sortByCreationDate");
    } else if (selectedIndex.value == 2) {
      window.location.replace("/sortByDueDate");
    }
  }

  function filterByState(selectedIndex){
    if (selectedIndex.value == 1) {
        window.location.replace("/filterByActive");
      } else if (selectedIndex.value == 2) {
        window.location.replace("/filterByCompleted");
      } else if (selectedIndex.value == 3) {
        window.location.replace("/filterByIsDue");
      }
  }

  function filterByTag(selectedIndex){
    window.location.replace("/tag/" + selectedIndex.value);
  }


  var isDarkMode = true;
  function switchTheme(){
    var css = document.getElementById("style");
    if(!isDarkMode) {
      css.disabled = undefined;
      isDarkMode = true;
    } else {
      css.disabled = "disabled";
      isDarkMode = false;
    }
  }