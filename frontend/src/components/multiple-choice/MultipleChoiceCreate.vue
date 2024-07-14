<template>
  <div class="multiple-choice-options">
    <v-row data-cy="questionIsToOrder">
      <v-col cols="1" offset="10">
        To Order
        <v-switch
          v-model="sQuestionDetails.isToOrder"
          inset
          :data-cy="`Switch`"
          @click="resetAllOrder"
        />
      </v-col>
    </v-row>
    <v-row>
      <v-col cols="1" offset="9">
        Correct
      </v-col>
      <v-col cols="1"
      v-if="sQuestionDetails.isToOrder"
      >
        Order
      </v-col>
    </v-row>
    <v-row
      v-for="(option, index) in sQuestionDetails.options"
      :key="index"
      data-cy="questionOptionsInput"
    >
      <v-col cols="9">
        <v-textarea
          v-model="option.content"
          :label="`Option ${index + 1}`"
          :data-cy="`Option${index + 1}`"
          rows="1"
          auto-grow
        ></v-textarea>
      </v-col>
      <v-col cols="1">
        <v-switch
          v-model="option.correct"
          inset
          :data-cy="`Switch${index + 1}`"
          @click="resetOrder(index)"
        />
      </v-col>
      <v-col v-if="sQuestionDetails.isToOrder" cols="1">
        <p v-if="option.correct">
          <v-select
            index
            :items="availableOptions"
            v-model="option.order"
            :disabled="readonlyEdit"
            :data-cy="`Order${index + 1}`"
          />
        <p/>       
      </v-col>
      <v-col v-if="sQuestionDetails.options.length > 2">
        <v-tooltip bottom>
          <template v-slot:activator="{ on }">
            <v-icon
              :data-cy="`Delete${index + 1}`"
              small
              class="ma-1 action-button"
              v-on="on"
              @click="removeOption(index)"
              color="red"
              >close</v-icon
            >
          </template>
          <span>Remove Option</span>
        </v-tooltip>
      </v-col>
    </v-row>

    <v-row>
      <v-btn
        class="ma-auto"
        color="blue darken-1"
        @click="addOption"
        data-cy="addOptionMultipleChoice"
        >Add Option</v-btn
      >
    </v-row>
  </div>
</template>

<script lang="ts">
import { Component, Model, PropSync, Vue, Watch } from 'vue-property-decorator';
import MultipleChoiceQuestionDetails from '@/models/management/questions/MultipleChoiceQuestionDetails';
import Option from '@/models/management/Option';

@Component
export default class MultipleChoiceCreate extends Vue {
  @PropSync('questionDetails', { type: MultipleChoiceQuestionDetails })
  sQuestionDetails!: MultipleChoiceQuestionDetails;

  addOption() {
    this.sQuestionDetails.options.push(new Option());
  }

  removeOption(index: number) {
    this.sQuestionDetails.options.splice(index, 1);
  }

  get availableOptions(): number[] {
    var array = new Array()
    var j=0;
    for(var i=0; i < this.sQuestionDetails.options.length;i++){
      if (this.sQuestionDetails.options[i].correct){
        j++;
        array.push(j);  
      }
    }
    return array;
  }

  resetAllOrder(){
    for(var i=0; i < this.sQuestionDetails.options.length;i++){
      this.sQuestionDetails.options[i].order = null;
    }
  }

  resetOrder(index: number){
    this.sQuestionDetails.options[index].order = null;
  }
  
}
</script>
