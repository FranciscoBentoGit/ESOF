<template>
  <div class="item-combination-options">



    <v-row>
      <v-col>
        <v-row
          v-for="(leftIcon, index) in sQuestionDetails.leftIcons"
          :key="index"
          data-cy="leftIconsInput"
        >
          <v-col cols="3">
          </v-col>
          <v-col v-if="sQuestionDetails.leftIcons.length > 1" cols="1">
            <v-tooltip bottom>
              <template v-slot:activator="{ on }">
                <v-icon
                  :data-cy="`Delete${index + 1}`"
                  small
                  class="ma-1 action-button"
                  v-on="on"
                  @click="removeLeftIcon(index)"
                  color="red"
                  >close</v-icon
                >
              </template>
              <span>Remove Option</span>
            </v-tooltip>
          </v-col>
          <v-col cols="4">
            <v-textarea
              v-model="leftIcon.content"
              :label="`Left Item ${index + 1}`"
              :data-cy="`leftIcons${index + 1}`"
              rows="1"
              auto-grow
            ></v-textarea>
          </v-col>
        </v-row>
      </v-col>
      <v-col>
        <v-row
          v-for="(rightIcon, index) in sQuestionDetails.rightIcons"
          :key="index"
          data-cy="rightIconsInput"
        >
            <v-col cols="3">
              <v-select
                multiple="true"
                :label="`Left Item Matches`"
                :items="getAvailableLeftIcons"
                v-model="rightIcon.match"
                :disabled="readonlyEdit"
                :data-cy="`matches${index + 1}`"
              />
            </v-col>
          <v-col 
          cols="4">
            <v-textarea
              v-model="sQuestionDetails.rightIcons[index].content"
              :label="`Right Item ${index + 1}`"
              :data-cy="`rightIcons${index + 1}`"
              rows="1"
              auto-grow
            ></v-textarea>
          </v-col>
          <v-col v-if="sQuestionDetails.rightIcons.length > 1">
            <v-tooltip bottom>
              <template v-slot:activator="{ on }">
                <v-icon
                  :data-cy="`Delete${index + 1}`"
                  small
                  class="ma-1 action-button"
                  v-on="on"
                  @click="removeRightIcon(index)"
                  color="red"
                  >close</v-icon
                >
              </template>
              <span>Remove Option</span>
            </v-tooltip>
          </v-col>
        </v-row>
      </v-col>
    </v-row>

    <v-row>
      <v-col>
        <v-layout align-center justify-center>
          <v-btn
          class="ma-auto"
          color="blue darken-1"
          @click="addLeftIcon"
          data-cy="addLeftIconItemCombination"
          >Add Left Item
          </v-btn>
        </v-layout>
      </v-col>
      <v-col>
        <v-layout align-center justify-center>
          <v-btn
          class="ma-auto"
          color="blue darken-1"
          @click="addRightIcon"
          data-cy="addRightIconItemCombination"
          >Add Right Item
          </v-btn>
        </v-layout>
      </v-col>
    </v-row>
  </div>
</template>

<script lang="ts">
import { Component, Model, PropSync, Vue, Watch } from 'vue-property-decorator';
import ItemCombinationQuestionDetails from '@/models/management/questions/ItemCombinationQuestionDetails';
import IconLeft from '@/models/management/IconLeft';
import IconRight from '@/models/management/IconRight';

@Component
export default class ItemCombinationCreate extends Vue {
  @PropSync('questionDetails', { type: ItemCombinationQuestionDetails })
  sQuestionDetails!: ItemCombinationQuestionDetails;

  addLeftIcon() {
    this.sQuestionDetails.leftIcons.push(new IconLeft());
  }

  addRightIcon() {
    this.sQuestionDetails.rightIcons.push(new IconRight());
  }

  removeLeftIcon(index: number) {
    this.sQuestionDetails.leftIcons.splice(index, 1);
  }

  removeRightIcon(index: number) {
    this.sQuestionDetails.rightIcons.splice(index, 1);
  }

  get getAvailableLeftIcons(): number[] {
    var array = new Array()
    var el = 0;
    for(var i = 0; i < this.sQuestionDetails.leftIcons.length;i++){
        el++;
        array.push(el);  
    }
    return array;
  }
}
</script>
