/*
  This function takes project status data in JSON format and plots a graph using
  dimple.js and D3.js in the designated div.
*/

function drawBudgetGraph(data) {
  // Prepares data and format dates
  var budget = JSON.parse(JSON.stringify(data));
  budget.forEach(function(d){ d.time = new Date(d.time) });

  // Sets up the dimple chart
  var svg = dimple.newSvg("#budgetChartContainer", 600, 430);
  var budgetGraph = new dimple.chart(svg, budget);
  budgetGraph.setBounds(70, 20, 420, 340);

  // Sets time and money axes
  var x = budgetGraph.addTimeAxis("x", "time", undefined,"%d/%m/%y");
  x.showGridlines = true;
  x.addOrderRule("Date");
  var y = budgetGraph.addMeasureAxis("y", "spentBudget");
  
  //Plots the budget data
  //TODO: show the proper information in data point labels
  budgetGraph.addSeries(["project"], dimple.plot.line);
  
  //Draws the graph and fixes axis labels
  budgetGraph.draw();
  x.titleShape.remove();
  y.titleShape.text("Spent Budget (€)");
}